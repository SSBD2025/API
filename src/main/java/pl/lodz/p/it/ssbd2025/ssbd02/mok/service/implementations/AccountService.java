package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations;

import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
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
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountRolesProjection;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.AccountMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.TokenEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.*;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.UserRole;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.*;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.AccessRole;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.*;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.InvalidCredentialsException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.UserRoleRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations.LockTokenService;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IAccountService;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IJwtService;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IPasswordResetTokenService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.*;

import java.security.SecureRandom;

import java.sql.SQLTransientConnectionException;
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
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
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
    @NotNull
    private final UserRoleRepository userRoleRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        if(!BCrypt.checkpw(changePasswordDTO.oldPassword(), account.getPassword())) {
            throw new InvalidCredentialsException();
        }
        accountRepository.updatePassword(account.getLogin(), BCrypt.hashpw(changePasswordDTO.newPassword(), BCrypt.gensalt()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    //TODO zastanowić się czy tutaj dać
    public String setGeneratedPassword(UUID uuid) { //TODO zmienić na void
        Optional<Account> account = accountRepository.findById(uuid);
        if(account.isEmpty()) {
            throw new AccountNotFoundException();
        }
        String password = generateRandomPassword();
        accountRepository.updatePassword(account.get().getLogin(), BCrypt.hashpw(password, BCrypt.gensalt()));
        String token = UUID.randomUUID().toString();
        passwordResetTokenService.createPasswordResetToken(account.get(), token);
        emailService.sendPasswordChangedByAdminEmail(account.get().getEmail(), account.get().getLogin(), account.get().getLanguage(), token, password);
        return password;
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
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", noRollbackFor = {InvalidCredentialsException.class, ExcessiveLoginAttemptsException.class})
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class,
            AccountNotFoundException.class, // teoretycznie w przypadku konta admina, ale bardzo watpliwe
            AccountHasNoRolesException.class, // te trzy w przypadku gdy nastapi zmiana wspolbieznie - admin nada/aktywuje role, odblokuje konto lub uzytkownik je zweryfikuje
            AccountNotActiveException.class,
            AccountNotVerifiedException.class
    }, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public TokenPairDTO login(String username, String password, String ipAddress) {
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
            accountRepository.updateSuccessfulLogin(username, currentTime, ipAddress, 0);
            if(account.isTwoFactorAuth()){
                emailService.sendTwoFactorCode(account.getEmail(), account.getLogin(), tokenUtil.generateTwoFactorCode(account), account.getLanguage());
                return new TokenPairDTO(null, null, true);
            }

            accountRepository.updateSuccessfulLogin(username, currentTime, ipAddress);
            if(userRoles.contains("ADMIN")) {
                emailService.sendAdminLoginEmail(account.getEmail(), account.getLogin(), ipAddress, account.getLanguage());
            }
            return jwtService.generatePair(account, userRoles);
        } else {
            if(account.getLoginAttempts() + 1 > maxLoginAttempts) {

                lockTemporarily(username, Timestamp.from(tokenUtil.generateMinuteExpiration(lockedFor).toInstant()));

                accountRepository.updateFailedLogin(username, currentTime, ipAddress, 0);
                throw new ExcessiveLoginAttemptsException();
            }
            accountRepository.updateFailedLogin(username, currentTime, ipAddress, account.getLoginAttempts() + 1);
            throw new InvalidCredentialsException();
        }
    }

//    @PreAuthorize("hasRole('SYSTEM')")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    protected void lockTemporarily(String username, Timestamp until){
        accountRepository.lockTemporarily(username, until);
    }


    @PreAuthorize("permitAll()")
    public TokenPairDTO verifyTwoFactorCode(String username, String code, String ipAddress) {
        Account account = accountRepository.findByLogin(username).orElseThrow(AccountNotFoundException::new);

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

        List<AccountRolesProjection> roles = accountRepository.findAccountRolesByLogin(username);
        List<String> userRoles = roles.stream()
                .filter(AccountRolesProjection::isActive)
                .map(AccountRolesProjection::getRoleName)
                .collect(Collectors.toList());

        accountRepository.updateSuccessfulLogin(username, new Date(), ipAddress);

        if (userRoles.contains("ADMIN")) {
            emailService.sendAdminLoginEmail(account.getEmail(), account.getLogin(), ipAddress, account.getLanguage());
        }

        return jwtService.generatePair(account, userRoles);
    }

    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void logout() {
        Account account = accountRepository.findByLogin(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(AccountNotFoundException::new);
        tokenRepository.deleteAllByAccountWithType(account, TokenType.ACCESS);
        tokenRepository.deleteAllByAccountWithType(account, TokenType.REFRESH);
        SecurityContextHolder.clearContext();
    }

    @PreAuthorize("hasRole('ADMIN')") //TODO co z system?
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void blockAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException());

        if (account.getLogin().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw new SelfBlockAccountException();
        }

        if (!account.isActive())
            throw new AccountAlreadyBlockedException();



        account.setActive(false);
        accountRepository.saveAndFlush(account);

//        emailService.sendBlockAccountEmail(account.getEmail(), account.getLogin(), account.getLanguage()); //TODO MAILE SIE ZMIENILY POPRAWIC!

    }

    @PreAuthorize("hasRole('ADMIN')") //TODO co z system?
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void unblockAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException());

        if (account.isActive())
            throw new AccountAlreadyUnblockedException();

        account.setActive(true);
        accountRepository.saveAndFlush(account);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    //TODO tu raczej bez powtarzania ale do ustalenia
    public void sendResetPasswordEmail(String email) {
        if(accountRepository.findByEmail(email).isPresent()) {
            Account account = accountRepository.findByEmail(email).get();
            String token = UUID.randomUUID().toString();
            passwordResetTokenService.createPasswordResetToken(account, token);
            emailService.sendResetPasswordEmail(account.getEmail(), account.getLogin(), account.getLanguage(), token);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
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
    @Transactional(readOnly = true, transactionManager = "mokTransactionManager")
    public List<AccountWithRolesDTO> getAllAccounts(Boolean active, Boolean verified) {
        List<Account> accounts = accountRepository.findByActiveAndVerified(active, verified);

        return accounts.stream()
                .map(accountMapper::toAccountWithUserRolesDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
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
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
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
        if (accountRepository.findByEmail(newEmail) != null) {
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
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
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
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
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
    @Transactional(readOnly = true, transactionManager = "mokTransactionManager")
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

    @Transactional(readOnly = true, transactionManager = "mokTransactionManager")
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


    @Transactional(readOnly = true, transactionManager = "mokTransactionManager")
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
    public void updateAccountById(UUID id, UpdateAccountDTO dto) {
        updateAccount(() -> accountRepository.findById(id)
                .orElseThrow(AccountNotFoundException::new), dto);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class, OptimisticLockException.class},
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
            throw new OptimisticLockException("Version123 mismatch");
        }

        account.setFirstName(dto.firstName());
        account.setLastName(dto.lastName());

        accountRepository.saveAndFlush(account);
    }

    //TODO co to?
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public void updateMyAccount(String login, UpdateAccountDTO dto) {
        updateAccount(() -> accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new), dto);
    }

    @PreAuthorize("permitAll()")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
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
            throw new AccountAlreadyVerifiedException(); //remember to set verified = false when changing email
        }
        tokenRepository.delete(verificationToken);
        account.setVerified(true);
    }

    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
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
    }

    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
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
    }

    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
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
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
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
