package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountRolesProjection;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.AccountMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.TokenEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.*;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.UserRole;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.AccessRole;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.*;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.InvalidCredentialsException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token.TokenExpiredException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token.TokenNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.RoleChangeLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.UserRoleChangeLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.ChangePasswordRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.UserRoleRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IAccountService;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IJwtService;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IPasswordResetTokenService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.*;

import java.sql.Timestamp;
import java.util.*;

import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.TokenUtil;

import java.util.ArrayList;
import java.util.List;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.UUID;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.ChangePasswordDTO;

import static pl.lodz.p.it.ssbd2025.ssbd02.utils.MiscellaneousUtil.generateRandomPassword;

@TransactionLogged
@Component
@RequiredArgsConstructor
@Service
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
public class AccountService implements IAccountService {

    @NotNull
    private final AccountRepository accountRepository;
    @NotNull
    private final TokenUtil tokenUtil;
    @NotNull
    private final JwtTokenProvider jwtTokenProvider;
    @NotNull
    private final EmailService emailService;
    @NotNull
    private final IJwtService jwtService;
    @NotNull
    private final IPasswordResetTokenService passwordResetTokenService;
    private final AccountMapper accountMapper;
    private final LockTokenService lockTokenService;
    private final TokenRepository tokenRepository;
    private final ChangePasswordRepository changePasswordRepository;
    @Value("${mail.confirmation.url}")
    private String confirmURL;
    @Value("${mail.revert.url}")
    private String revertURL;
    @Value("${app.login.maxAttempts}")
    private int maxLoginAttempts;
    @Value("${app.login.lockedFor}")
    private int lockedFor;
    @Value("${app.jwt_refresh_expiration}")
    private int jwtRefreshExpiration;
    @Value("${mail.unlock.url}")
    private String unlockURL;
    @NotNull
    private final UserRoleRepository userRoleRepository;
    @NotNull
    private final SseEmitterManager emitterManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", readOnly = false, timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        if(!BCrypt.checkpw(changePasswordDTO.getOldPassword(), account.getPassword())) {
            throw new InvalidCredentialsException();
        }
        for (String prevPassword : account.getPreviousPasswords()) {
            if (BCrypt.checkpw(changePasswordDTO.getNewPassword(), prevPassword)) {
                throw new PreviousPasswordUsedException();
            }
        }
        account.getPreviousPasswords().add(account.getPassword());
        account.setPassword(BCrypt.hashpw(changePasswordDTO.getNewPassword(), BCrypt.gensalt()));
//        accountRepository.updatePassword(account.getLogin(), BCrypt.hashpw(changePasswordDTO.getNewPassword(), BCrypt.gensalt()));
        accountRepository.saveAndFlush(account);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", readOnly = false, timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("permitAll()")
    public void forceChangePassword(ForceChangePasswordDTO forceChangePasswordDTO) {
        String login = forceChangePasswordDTO.getLogin();
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        if(!BCrypt.checkpw(forceChangePasswordDTO.getOldPassword(), account.getPassword())) {
            throw new InvalidCredentialsException();
        }
        ChangePasswordEntity changePasswordEntity = changePasswordRepository.findByAccount(account);
        changePasswordEntity.setPasswordToChange(false);
        changePasswordRepository.saveAndFlush(changePasswordEntity);
        account.setPassword(BCrypt.hashpw(forceChangePasswordDTO.getNewPassword(), BCrypt.gensalt()));
        accountRepository.saveAndFlush(account);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", readOnly = false, timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('ADMIN')")
    public void setGeneratedPassword(UUID uuid) {
        Optional<Account> account = accountRepository.findById(uuid);
        if(account.isEmpty()) {
            throw new AccountNotFoundException();
        }
        String password = generateRandomPassword();
        account.get().setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        accountRepository.saveAndFlush(account.get());
        ChangePasswordEntity changePasswordEntity = changePasswordRepository.findByAccount(account.get());
        changePasswordEntity.setPasswordToChange(true);
        changePasswordRepository.saveAndFlush(changePasswordEntity);
        String token = UUID.randomUUID().toString();
        passwordResetTokenService.createPasswordResetToken(account.get(), new SensitiveDTO(token));
        emailService.sendPasswordChangedByAdminEmail(account.get().getEmail(), account.get().getLogin(), account.get().getLanguage(), new SensitiveDTO(token), password);
    }

    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}",
            noRollbackFor = {InvalidCredentialsException.class, ExcessiveLoginAttemptsException.class, AccountIsAutolockedException.class})
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class,
    }, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public SensitiveDTO login(String username, SensitiveDTO password, String ipAddress, HttpServletResponse response) {
        Account account = accountRepository.findByLogin(username).orElseThrow(AccountNotFoundException::new);
        List<AccountRolesProjection> roles = accountRepository.findAccountRolesByLogin(username);
        List<String> userRoles = new ArrayList<>();
        if(roles.isEmpty()) {
            throw new AccountHasNoRolesException();
        }
        roles.forEach(role -> {
            if (role.isActive()) {
                userRoles.add(role.getRoleName());
            }
        });
        if(userRoles.isEmpty()) {
            throw new AccountHasNoRolesException();
        }
        if (!account.isActive()) {
            throw new AccountNotActiveException();
        }
        if (!account.isVerified()) {
            throw new AccountNotVerifiedException();
        }
        if (account.isAutoLocked()) {
            SensitiveDTO dto = tokenUtil.generateAutoLockUnlockCode(account);
            emailService.sendAutolockUnlockLink(account.getLogin(), account.getEmail(), new SensitiveDTO(unlockURL+dto.getValue()), account.getLanguage());
            throw new AccountIsAutolockedException();
        }
        Date currentTime = new Date(System.currentTimeMillis());
        if (tokenUtil.checkPassword(password, account.getPassword())) {
            String acceptLanguage = MiscellaneousUtil.getAcceptLanguage();
            if (acceptLanguage != null) {
                Language newLanguage = acceptLanguage.toLowerCase().contains("pl") ? Language.pl_PL : Language.en_EN;
                if (account.getLanguage() != newLanguage) {
                    account.setLanguage(newLanguage);
                    accountRepository.saveAndFlush(account);
                }
            }

            accountRepository.updateSuccessfulLogin(username, currentTime, ipAddress, 0);
            if(account.isTwoFactorAuth()){
                emailService.sendTwoFactorCode(account.getEmail(), account.getLogin(), tokenUtil.generateTwoFactorCode(account), account.getLanguage());

                SensitiveDTO access2FAToken = jwtTokenProvider.generateAccess2FAToken(account);

                tokenRepository.saveAndFlush(new TokenEntity(access2FAToken.getValue(), jwtTokenProvider.getExpiration(access2FAToken), account, TokenType.ACCESS_2FA));

                return access2FAToken;
            }
            if(userRoles.contains("ADMIN")) {
                emailService.sendAdminLoginEmail(account.getEmail(), account.getLogin(), ipAddress, account.getLanguage());
            }
            TokenPairDTO pair = jwtService.generatePair(account, userRoles);
            String access = pair.getAccessToken();
            String refresh = pair.getRefreshToken();
            if(changePasswordRepository.findByAccount(account).isPasswordToChange()) {
                throw new PasswordToChangeException();
            }
            jwtTokenProvider.cookieSetter(new SensitiveDTO(refresh), jwtRefreshExpiration, response);

            return new SensitiveDTO(access);
        } else {
            if(account.getLoginAttempts() + 1 >= maxLoginAttempts) {
                accountRepository.lockTemporarily(username, Timestamp.from(tokenUtil.generateMinuteExpiration(lockedFor).toInstant()));
                accountRepository.updateFailedLogin(username, currentTime, ipAddress, 0);
                throw new ExcessiveLoginAttemptsException();
            }
            accountRepository.updateFailedLogin(username, currentTime, ipAddress, account.getLoginAttempts() + 1);
            throw new InvalidCredentialsException();
        }
    }

    @PreAuthorize("hasAuthority('2FA_AUTHORITY')")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public SensitiveDTO verifyTwoFactorCode(SensitiveDTO code, String ipAddress, HttpServletResponse response) {
        Account account = accountRepository.findByLogin(SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal().toString()).orElseThrow(AccountNotFoundException::new);

        List<TokenEntity> tokens = tokenRepository.findAllByAccountWithType(account, TokenType.TWO_FACTOR);
        if (tokens.isEmpty()) {
            throw new TokenNotFoundException();
        }

        TokenEntity token = tokens.getFirst();

        if (token.getExpiration().before(new Date())) {
            tokenRepository.delete(token);
            throw new TokenExpiredException();
        }

        boolean isValid = BCrypt.checkpw(code.getValue(), token.getToken());
        if (!isValid) {
            throw new TwoFactorTokenInvalidException();
        }

        tokenRepository.delete(token);

        List<AccountRolesProjection> roles = accountRepository.findAccountRolesByLogin(account.getLogin());
        List<String> userRoles = roles.stream()
                .filter(AccountRolesProjection::isActive)
                .map(AccountRolesProjection::getRoleName)
                .collect(Collectors.toList());

        accountRepository.updateSuccessfulLogin(account.getLogin(), new Date(), ipAddress, 0);

        if (userRoles.contains("ADMIN")) {
            emailService.sendAdminLoginEmail(account.getEmail(), account.getLogin(), ipAddress, account.getLanguage());
        }

        TokenPairDTO pair = jwtService.generatePair(account, userRoles);

        jwtTokenProvider.cookieSetter(new SensitiveDTO(pair.getRefreshToken()), jwtRefreshExpiration, response);


        return new SensitiveDTO(pair.getAccessToken());
    }

    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression
            = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void logout(HttpServletResponse response) {
        Account account = accountRepository.findByLogin(SecurityContextHolder.getContext().getAuthentication()
                .getName()).orElseThrow(AccountNotFoundException::new);
        tokenRepository.deleteAllByAccountWithType(account, TokenType.ACCESS);
        tokenRepository.deleteAllByAccountWithType(account, TokenType.REFRESH);
        jwtTokenProvider.cookieClear(response);
        SecurityContextHolder.clearContext();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(
            delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void blockAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(AccountNotFoundException::new);

        if (account.getLogin().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw new SelfBlockAccountException();
        }

        if (!account.isActive())
            throw new AccountAlreadyBlockedException();

        account.setActive(false);
        accountRepository.saveAndFlush(account);
        emitterManager.sendBlockedNotification(id);
        emailService.sendBlockAccountEmail(account.getEmail(), account.getLogin(), account.getLanguage());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(
            delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void unblockAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(AccountNotFoundException::new);

        if (account.isActive())
            throw new AccountAlreadyUnblockedException();

        account.setActive(true);
        accountRepository.saveAndFlush(account);
        emitterManager.sendUnblockedNotification(id);
        emailService.sendUnblockAccountEmail(account.getEmail(), account.getLogin(), account.getLanguage());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("permitAll()")
    public void sendResetPasswordEmail(String email) {
        if(accountRepository.findByEmail(email).isPresent()) {
            Account account = accountRepository.findByEmail(email).get();
            String token = UUID.randomUUID().toString();
            passwordResetTokenService.createPasswordResetToken(account, new SensitiveDTO(token));
            emailService.sendResetPasswordEmail(account.getEmail(), account.getLogin(), account.getLanguage(), new SensitiveDTO(token));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", readOnly = false, timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(
            delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("permitAll()")
    public void resetPassword(SensitiveDTO token, ResetPasswordDTO resetPasswordDTO) {
        TokenEntity tokenEntity = tokenRepository.findByToken(token.getValue()).orElseThrow(TokenNotFoundException::new);
        passwordResetTokenService.validatePasswordResetToken(token);
        Account account = tokenEntity.getAccount();
        if(account == null) {
            throw new AccountNotFoundException();
        }
        for (String prevPassword : account.getPreviousPasswords()) {
            if (BCrypt.checkpw(resetPasswordDTO.getPassword(), prevPassword)) {
                throw new PreviousPasswordUsedException();
            }
        }
        ChangePasswordEntity changePasswordEntity = changePasswordRepository.findByAccount(account);
        changePasswordEntity.setPasswordToChange(false);
        changePasswordRepository.saveAndFlush(changePasswordEntity);
        account.getPreviousPasswords().add(account.getPassword());
        account.setPassword(BCrypt.hashpw(resetPasswordDTO.getPassword(), BCrypt.gensalt()));
        accountRepository.saveAndFlush(account);
    }

    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AccountWithRolesDTO> getAllAccounts(Boolean active, Boolean verified, String searchPhrase, Pageable pageable) {
        Page<Account> accounts;

        if (searchPhrase != null && !searchPhrase.isEmpty()) {
            accounts = accountRepository.findByActiveAndVerifiedAndNameContaining(active, verified, searchPhrase, pageable);
        } else {
            accounts = accountRepository.findByActiveAndVerified(active, verified, pageable);
        }

        return accounts.map(accountMapper::toAccountWithUserRolesDTO);
    }

    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class
    }, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void changeOwnEmail(String newEmail) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        handleEmailChange(account, newEmail);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class
    }, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void changeUserEmail(UUID accountId, String newEmail) {
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        handleEmailChange(account, newEmail);
    }

    private void handleEmailChange(Account account, String newEmail) {
        if (account.getEmail().equals(newEmail)) {
            throw new AccountSameEmailException();
        }
        if (accountRepository.findByEmail(newEmail).isPresent()) {
            throw new AccountEmailAlreadyInUseException();
        }

        SensitiveDTO emailChangeToken = jwtTokenProvider.generateEmailChangeToken(account, newEmail);
        String confirmationURL = confirmURL + emailChangeToken.getValue();

        TokenEntity jwt = new TokenEntity(emailChangeToken.getValue(), jwtTokenProvider.getExpiration(emailChangeToken), account, TokenType.EMAIL_CHANGE);
        tokenRepository.saveAndFlush(jwt);

        emailService.sendChangeEmail(account.getLogin(), newEmail, new SensitiveDTO(confirmationURL), account.getLanguage());
    }

    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class
    }, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void confirmEmail(SensitiveDTO token) {
        TokenEntity jwt = tokenRepository.findByToken(token.getValue()).orElseThrow(TokenNotFoundException::new);
        if (jwt.getExpiration().before(new Date())) {
            tokenRepository.delete(jwt);
            throw new TokenExpiredException();
        }

        String newEmail = jwtTokenProvider.getNewEmailFromToken(token);
        UUID accountId = jwtTokenProvider.getAccountIdFromToken(token);

        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);

        String oldEmail = account.getEmail();

        account.setEmail(newEmail);
        accountRepository.saveAndFlush(account);
        tokenRepository.delete(jwt);

        SensitiveDTO revertToken = jwtTokenProvider.generateEmailRevertToken(account, oldEmail);
        String revertChangeURL = revertURL + revertToken.getValue();

        TokenEntity tokenEntity = new TokenEntity(revertToken.getValue(), jwtTokenProvider.getExpiration(revertToken), account, TokenType.EMAIL_REVERT);
        tokenRepository.saveAndFlush(tokenEntity);

        emailService.sendRevertChangeEmail(account.getLogin(), oldEmail, new SensitiveDTO(revertChangeURL), account.getLanguage());
    }

    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class
    }, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void revertEmailChange(SensitiveDTO token) {
        TokenEntity jwt = tokenRepository.findByToken(token.getValue()).orElseThrow(TokenNotFoundException::new);
        if (jwt.getExpiration().before(new Date())) {
            tokenRepository.delete(jwt);
            throw new TokenExpiredException();
        }

        String oldEmail = jwtTokenProvider.getOldEmailFromToken(token);
        UUID accountId = jwtTokenProvider.getAccountIdFromToken(token);

        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);

        account.setEmail(oldEmail);
        accountRepository.saveAndFlush(account);
        tokenRepository.delete(jwt);
    }

    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public void resendEmailChangeLink() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        List<TokenEntity> tokens = tokenRepository.findAllByAccountWithType(account, TokenType.EMAIL_CHANGE);
        TokenEntity tokenEntity = tokens.stream()
                .filter(t -> t.getExpiration().after(new Date()))
                .max(Comparator.comparing(TokenEntity::getExpiration))
                .orElseThrow(EmailChangeTokenNotFoundException::new);

        String emailChangeToken = tokenEntity.getToken();
        String newEmail = jwtTokenProvider.getNewEmailFromToken(new SensitiveDTO(emailChangeToken));
        String confirmationURL = confirmURL + emailChangeToken;

        emailService.sendChangeEmail(account.getLogin(), newEmail, new SensitiveDTO(confirmationURL), account.getLanguage());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public AccountWithTokenDTO getAccountByLogin(String login) {
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        List<AccountRolesProjection> projections = accountRepository.findAccountRolesByLogin(login);
        List<AccountRoleDTO> roles = projections.stream()
                .map(p -> new AccountRoleDTO(p.getRoleName(), p.isActive()))
                .toList();

        AccountDTO dto = accountMapper.toAccountDTO(account);
        SensitiveDTO token = lockTokenService.generateToken(account.getId(), account.getVersion());

        return new AccountWithTokenDTO(dto, token.getValue(), roles);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public AccountWithTokenDTO getAccountById(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(AccountNotFoundException::new);

        AccountDTO dto = accountMapper.toAccountDTO(account);
        SensitiveDTO token = lockTokenService.generateToken(account.getId(), account.getVersion());
        List<AccountRolesProjection> projections = accountRepository.findAccountRolesByLogin(account.getLogin());

        List<AccountRoleDTO> roles = projections.stream()
                .map(p -> new AccountRoleDTO(p.getRoleName(), p.isActive()))
                .toList();

        return new AccountWithTokenDTO(dto, token.getValue(), roles);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public void updateAccountById(UUID id, UpdateAccountDTO dto) {
        updateAccount(() -> accountRepository.findById(id)
                .orElseThrow(AccountNotFoundException::new), dto);
    }

    @UserRoleChangeLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public void logUserRoleChange(String login, String previousRole, String newRole) {
        accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
    }

    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false, transactionManager = "mokTransactionManager")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}"
    )
    public void updateAccount(Supplier<Account> accountSupplier, UpdateAccountDTO dto) {
        LockTokenService.Record<UUID, Long> record = lockTokenService.verifyToken(dto.getLockToken());

        Account account = accountSupplier.get();

        if (!account.isActive()) {
            throw new AccountNotActiveException();
        }

        if (!account.getId().equals(record.id())) {
            throw new InvalidLockTokenException();
        }

        if (!Objects.equals(account.getVersion(), record.version())) {
            throw new ConcurrentUpdateException();
        }

        account.setFirstName(dto.getFirstName());
        account.setLastName(dto.getLastName());

        accountRepository.saveAndFlush(account);
    }

    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public void updateMyAccount(String login, UpdateAccountDTO dto) {
        updateAccount(() -> accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new), dto);
    }

    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(
            delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void verifyAccount(SensitiveDTO token) {
        TokenEntity verificationToken = tokenRepository.findByToken(token.getValue()).orElseThrow(TokenNotFoundException::new);
        if(verificationToken.getExpiration().before(new Date())) {
            throw new TokenExpiredException();
        }
        Account account = verificationToken.getAccount();
        if(account == null) {
            throw new AccountNotFoundException();
        }
        if(account.isVerified()){
            throw new AccountAlreadyVerifiedException();
        }
        tokenRepository.delete(verificationToken);
        account.setVerified(true);
        accountRepository.saveAndFlush(account);
        emailService.sendActivateAccountEmail(account.getEmail(), account.getLogin(), account.getLanguage());
    }

    @RoleChangeLogged
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class,
    }, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void assignRole(UUID accountId, AccessRole accessRole, String login) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        if (Objects.equals(login, account.getLogin())) {
            throw new SelfRoleAssignmentException();
        }

        String roleName = accessRole.name();

        if (accessRole == AccessRole.DIETICIAN) {
            Optional<Boolean> clientRoleActiveOpt = userRoleRepository.findActiveByLoginAndRoleName(account.getLogin(),
                    AccessRole.CLIENT.name());
            if (clientRoleActiveOpt.orElse(false)) {
                throw new RoleConflictException();
            }
        } else if (accessRole == AccessRole.CLIENT) {
            Optional<Boolean> dieticianRoleActiveOpt = userRoleRepository.findActiveByLoginAndRoleName(account.getLogin(),
                    AccessRole.DIETICIAN.name());
            if (dieticianRoleActiveOpt.orElse(false)) {
                throw new RoleConflictException();
            }
        }

        boolean exists = userRoleRepository.existsByLoginAndRoleName(account.getLogin(), roleName);

        if (exists) {
            Optional<Boolean> activeOpt = userRoleRepository.findActiveByLoginAndRoleName(account.getLogin(), roleName);
            if (activeOpt.isPresent() && !activeOpt.get()) {
                userRoleRepository.updateRoleActiveStatus(account.getLogin(), roleName, true);
            }
        } else {
            UserRole newRole = switch (accessRole) {
                case ADMIN -> new Admin();
                case DIETICIAN -> new Dietician();
                case CLIENT -> new Client();
            };
            newRole.setAccount(account);
            newRole.setActive(true);
            userRoleRepository.saveAndFlush(newRole);
        }

        emailService.sendRoleAssignedEmail(account.getEmail(), account.getLogin(), accessRole.name(), account.getLanguage());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RoleChangeLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class,
    }, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void unassignRole(UUID accountId, AccessRole accessRole, String login) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);
        if (Objects.equals(account.getLogin(), login)) {
            throw new SelfRoleAssignmentException();
        }
        String roleName = accessRole.name();

        List<AccountRolesProjection> roles = accountRepository.findAccountRolesByLogin(account.getLogin());

        boolean hasActive = roles.stream()
                .anyMatch(r -> r.getRoleName().equals(roleName) && r.isActive());
        if (!hasActive) {
            throw new RoleNotFoundException();
        }

        userRoleRepository.updateRoleActiveStatus(account.getLogin(), roleName, false);
        emailService.sendRoleUnassignedEmail(account.getEmail(), account.getLogin(), accessRole.name(), account.getLanguage());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", readOnly = false, timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(
            delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("permitAll()")
    public void enableTwoFactor() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        if (account.isTwoFactorAuth())
            throw new AccountTwoFactorAlreadyEnabled();
        account.setTwoFactorAuth(true);
        accountRepository.saveAndFlush(account);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", readOnly = false, timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(
            delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("permitAll()")
    public void disableTwoFactor() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        if (!account.isTwoFactorAuth())
            throw new AccountTwoFactorAlreadyDisabled();
        account.setTwoFactorAuth(false);
        accountRepository.saveAndFlush(account);
    }

//    @PreAuthorize("permitAll()")
//    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
//    public void unlockAccountRequest(ChangeEmailDTO changeEmailDTO) {
//        Account account = accountRepository.findByEmail(changeEmailDTO.getEmail()).orElseThrow(AccountNotFoundException::new);
//        if(account.isAutoLocked() && !account.isActive()) {
//            SensitiveDTO dto = tokenUtil.generateAutoLockUnlockCode(account);
//            emailService.sendAutolockUnlockLink(account.getLogin(), account.getEmail(), new SensitiveDTO(unlockURL+dto.getValue()), account.getLanguage());
//        } else {
//            throw new AccountAutolockUnlockAttemptException();
//        }
//    }

    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public void unlockAccount(SensitiveDTO token) {
        TokenEntity code = tokenRepository.findByToken(token.getValue()).orElseThrow(TokenNotFoundException::new);
        if (code.getExpiration().before(new Date())) {
            tokenRepository.delete(code);
            throw new TokenExpiredException();
        }
        Account account = code.getAccount();
        account.setAutoLocked(false);
        account.setActive(true);
        tokenRepository.deleteAllByAccountWithType(account, TokenType.UNLOCK_CODE);
    }

    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public void authWithEmailRequest(ChangeEmailDTO changeEmailDTO){
        Account account = accountRepository.findByEmail(changeEmailDTO.getEmail()).orElseThrow(AccountNotFoundException::new);
        if(account.isActive() && account.isVerified()) {
            SensitiveDTO dto = tokenUtil.generateAutoLockUnlockCode(account);
            emailService.sendEmailAuth(account.getLogin(), account.getEmail(), new SensitiveDTO(dto.getValue()), account.getLanguage());
        } else {
            throw new AccountAutolockUnlockAttemptException();
        }
    }

    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public SensitiveDTO authWithEmail(SensitiveDTO token, String ipAddress, HttpServletResponse response) {
        TokenEntity tokenEntity = tokenRepository.findByToken(token.getValue()).orElseThrow(TokenNotFoundException::new);
        Account account = tokenEntity.getAccount();
        String username = account.getLogin();
        List<AccountRolesProjection> roles = accountRepository.findAccountRolesByLogin(username);
        List<String> userRoles = new ArrayList<>();
        if(roles.isEmpty()) {
            throw new AccountHasNoRolesException();
        }
        roles.forEach(role -> {
            if (role.isActive()) {
                userRoles.add(role.getRoleName());
            }
        });
        if(userRoles.isEmpty()) {
            throw new AccountHasNoRolesException();
        }
        if (!account.isActive()) {
            throw new AccountNotActiveException();
        }
        if (!account.isVerified()) {
            throw new AccountNotVerifiedException();
        }
        Date currentTime = new Date(System.currentTimeMillis());
        String acceptLanguage = MiscellaneousUtil.getAcceptLanguage();
        if (acceptLanguage != null) {
            Language newLanguage = acceptLanguage.toLowerCase().contains("pl") ? Language.pl_PL : Language.en_EN;
            if (account.getLanguage() != newLanguage) {
                account.setLanguage(newLanguage);
                accountRepository.saveAndFlush(account);
            }
        }
        tokenRepository.deleteAllByAccountWithType(account, TokenType.UNLOCK_CODE);
        tokenRepository.deleteAllByAccountWithType(account, TokenType.ACCESS);
        tokenRepository.deleteAllByAccountWithType(account, TokenType.REFRESH);
        accountRepository.updateSuccessfulLogin(username, currentTime, ipAddress, 0);
        if(userRoles.contains("ADMIN")) {
            emailService.sendAdminLoginEmail(account.getEmail(), account.getLogin(), ipAddress, account.getLanguage());
        }
        String access = jwtService.generateAccess(account, userRoles).getValue();
        String refresh = jwtService.generateShorterRefresh(account, userRoles).getValue();
        jwtTokenProvider.cookieSetter(new SensitiveDTO(refresh), jwtRefreshExpiration/2, response);

        return new SensitiveDTO(access);
    }
}
