package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
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
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.UserRoleRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IAccountService;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IJwtService;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IPasswordResetTokenService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.*;

import java.security.SecureRandom;

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
    @NotNull
    private final UserRoleRepository userRoleRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", readOnly = false, timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        if(!BCrypt.checkpw(changePasswordDTO.oldPassword(), account.getPassword())) {
            throw new InvalidCredentialsException();
        }
        accountRepository.updatePassword(account.getLogin(), BCrypt.hashpw(changePasswordDTO.newPassword(), BCrypt.gensalt()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", readOnly = false, timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('ADMIN')")
    public void setGeneratedPassword(UUID uuid) {
        Optional<Account> account = accountRepository.findById(uuid);
        if(account.isEmpty()) {
            throw new AccountNotFoundException();
        }
        String password = generateRandomPassword();
        accountRepository.updatePassword(account.get().getLogin(), BCrypt.hashpw(password, BCrypt.gensalt()));
        String token = UUID.randomUUID().toString();
        passwordResetTokenService.createPasswordResetToken(account.get(), token);
        emailService.sendPasswordChangedByAdminEmail(account.get().getEmail(), account.get().getLogin(), account.get().getLanguage(), token, password);
    }

    private String generateRandomPassword() {
        final int length = 12;
        SecureRandom random = new SecureRandom();

        return random.ints(length,33, 127)
                .mapToObj(i -> String.valueOf((char) i))
                .collect(Collectors.joining());
    }

    @PreAuthorize("permitAll()")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}",
            noRollbackFor = {InvalidCredentialsException.class, ExcessiveLoginAttemptsException.class})
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class,
    }, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public SensitiveDTO login(String username, String password, String ipAddress, HttpServletResponse response) {
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
        if (!account.isVerified()) { //uncomment later
            throw new AccountNotVerifiedException();
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

                String access2FAToken = jwtTokenProvider.generateAccess2FAToken(account);

                tokenRepository.saveAndFlush(new TokenEntity(access2FAToken, jwtTokenProvider.getExpiration(access2FAToken), account, TokenType.ACCESS_2FA));

                return new SensitiveDTO(jwtTokenProvider.generateAccess2FAToken(account));
            }
            if(userRoles.contains("ADMIN")) {
                emailService.sendAdminLoginEmail(account.getEmail(), account.getLogin(), ipAddress, account.getLanguage());
            }
            TokenPairDTO pair = jwtService.generatePair(account, userRoles);
            String access = pair.accessToken();
            String refresh = pair.refreshToken();

            jwtTokenProvider.cookieSetter(refresh, jwtRefreshExpiration, response);

            return new SensitiveDTO(access);
        } else {
            if(account.getLoginAttempts() + 1 >= maxLoginAttempts) {
                lockTemporarily(username, Timestamp.from(tokenUtil.generateMinuteExpiration(lockedFor).toInstant()));
                accountRepository.updateFailedLogin(username, currentTime, ipAddress, 0);
                throw new ExcessiveLoginAttemptsException();
            }
            accountRepository.updateFailedLogin(username, currentTime, ipAddress, account.getLoginAttempts() + 1);
            throw new InvalidCredentialsException();
        }
    }

    @PreAuthorize("permitAll()")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression
            = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    protected void lockTemporarily(String username, Timestamp until){
        accountRepository.lockTemporarily(username, until);
    }


    @PreAuthorize("hasAuthority('2FA_AUTHORITY')")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public SensitiveDTO verifyTwoFactorCode(String code, String ipAddress, HttpServletResponse response) {
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

        boolean isValid = BCrypt.checkpw(code, token.getToken());
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

        jwtTokenProvider.cookieSetter(pair.refreshToken(), jwtRefreshExpiration, response);


        return new SensitiveDTO(pair.accessToken());
    }

    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @TransactionLogged
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
    @TransactionLogged
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

        emailService.sendBlockAccountEmail(account.getEmail(), account.getLogin(), account.getLanguage());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @TransactionLogged
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

        emailService.sendUnblockAccountEmail(account.getEmail(), account.getLogin(), account.getLanguage());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("permitAll()")
    public void sendResetPasswordEmail(String email) {
        if(accountRepository.findByEmail(email).isPresent()) {
            Account account = accountRepository.findByEmail(email).get();
            String token = UUID.randomUUID().toString();
            passwordResetTokenService.createPasswordResetToken(account, token);
            emailService.sendResetPasswordEmail(account.getEmail(), account.getLogin(), account.getLanguage(), token);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", readOnly = false, timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(
            delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("permitAll()")
    public void resetPassword(String token, ResetPasswordDTO resetPasswordDTO) {
        TokenEntity tokenEntity = tokenRepository.findByToken(token).orElseThrow(TokenNotFoundException::new);
        passwordResetTokenService.validatePasswordResetToken(token);
        Account account = tokenEntity.getAccount();
        if(account == null) {
            throw new AccountNotFoundException();
        }
        accountRepository.updatePassword(account.getLogin(), BCrypt.hashpw(resetPasswordDTO.password(), BCrypt.gensalt()));
    }

    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AccountWithRolesDTO> getAllAccounts(Boolean active, Boolean verified) {
        List<Account> accounts = accountRepository.findByActiveAndVerified(active, verified);

        return accounts.stream()
                .map(accountMapper::toAccountWithUserRolesDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @TransactionLogged
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
    @TransactionLogged
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

        String emailChangeToken = jwtTokenProvider.generateEmailChangeToken(account, newEmail);
        String confirmationURL = confirmURL + emailChangeToken;

        TokenEntity jwt = new TokenEntity(emailChangeToken, jwtTokenProvider.getExpiration(emailChangeToken), account, TokenType.EMAIL_CHANGE);
        tokenRepository.save(jwt);

        emailService.sendChangeEmail(account.getLogin(), newEmail, confirmationURL, account.getLanguage());
    }

    @PreAuthorize("permitAll()")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class
    }, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void confirmEmail(String token) {
        TokenEntity jwt = tokenRepository.findByToken(token).orElseThrow(TokenNotFoundException::new);
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

        String revertToken = jwtTokenProvider.generateEmailRevertToken(account, oldEmail);
        String revertChangeURL = revertURL + revertToken;

        TokenEntity tokenEntity = new TokenEntity(revertToken, jwtTokenProvider.getExpiration(revertToken), account, TokenType.EMAIL_REVERT);
        tokenRepository.save(tokenEntity);

        emailService.sendRevertChangeEmail(account.getLogin(), oldEmail, revertChangeURL, account.getLanguage());
    }

    @PreAuthorize("permitAll()")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class
    }, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void revertEmailChange(String token) {
        TokenEntity jwt = tokenRepository.findByToken(token).orElseThrow(TokenNotFoundException::new);
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
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public void resendEmailChangeLink() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        TokenEntity tokenEntity = tokenRepository.findByType(TokenType.EMAIL_CHANGE);

        if (tokenEntity == null) {
            throw new EmailChangeTokenNotFoundException();
        }

        String emailChangeToken = tokenEntity.getToken();
        String newEmail = jwtTokenProvider.getNewEmailFromToken(emailChangeToken);
        String confirmationURL = confirmURL + emailChangeToken;

        emailService.sendChangeEmail(account.getLogin(), newEmail, confirmationURL, account.getLanguage());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @TransactionLogged
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public AccountWithTokenDTO getAccountByLogin(String login) {
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        List<AccountRolesProjection> projections = accountRepository.findAccountRolesByLogin(login);
        List<AccountRoleDTO> roles = projections.stream()
                .map(p -> new AccountRoleDTO(p.getRoleName(), p.isActive()))
                .toList();

        AccountDTO dto = accountMapper.toAccountDTO(account);
        String token = lockTokenService.generateToken(account.getId(), account.getVersion());

        return new AccountWithTokenDTO(dto, token, roles);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public AccountWithTokenDTO getAccountById(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(AccountNotFoundException::new);

        AccountDTO dto = accountMapper.toAccountDTO(account);
        String token = lockTokenService.generateToken(account.getId(), account.getVersion());
        List<AccountRolesProjection> projections = accountRepository.findAccountRolesByLogin(account.getLogin());

        List<AccountRoleDTO> roles = projections.stream()
                .map(p -> new AccountRoleDTO(p.getRoleName(), p.isActive()))
                .toList();

        return new AccountWithTokenDTO(dto, token, roles);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public void updateAccountById(UUID id, UpdateAccountDTO dto) {
        updateAccount(() -> accountRepository.findById(id)
                .orElseThrow(AccountNotFoundException::new), dto);
    }

    @UserRoleChangeLogged
    @TransactionLogged
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
        LockTokenService.Record<UUID, Long> record = lockTokenService.verifyToken(dto.lockToken());

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

        account.setFirstName(dto.firstName());
        account.setLastName(dto.lastName());

        accountRepository.saveAndFlush(account);
    }

    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public void updateMyAccount(String login, UpdateAccountDTO dto) {
        updateAccount(() -> accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new), dto);
    }

    @PreAuthorize("permitAll()")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(
            delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void verifyAccount(String token) {
        TokenEntity verificationToken = tokenRepository.findByToken(token).orElseThrow(TokenNotFoundException::new);
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
    @TransactionLogged
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
    @TransactionLogged
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

    @TransactionLogged
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

    @TransactionLogged
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
}
