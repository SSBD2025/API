package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import jakarta.persistence.OptimisticLockException;
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
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.*;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.InvalidCredentialsException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.UserRoleRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.*;

import java.security.SecureRandom;

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
public class AccountService {

    @NotNull
    private final AccountRepository accountRepository;
    @NotNull
    private final TokenUtil tokenUtil;
    @NotNull
    private final JwtTokenProvider jwtTokenProvider;
    @NotNull
    private final EmailService emailService;
    @NotNull
    private final JwtService jwtService;
    @NotNull
    private final PasswordResetTokenService passwordResetTokenService;
    private final AccountMapper accountMapper;
    private final LockTokenService lockTokenService;
    private final TokenRepository tokenRepository;
    @Value("${mail.confirmation.url}")
    private String confirmURL;
    @Value("${mail.revert.url}")
    private String revertURL;
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
//        emailService.sendPasswordChangedByAdminEmail(account.get().getEmail(), account.get().getLogin(), account.get().getLanguage(), token, password); //TODO MAILE SIE ZMIENILY POPRAWIC!
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
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
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
//        if (!account.isVerified()) { //uncomment later
//            throw new AccountNotVerifiedException();
//        }
        Date currentTime = new Date(System.currentTimeMillis());
        if (tokenUtil.checkPassword(password, account.getPassword())) {
            accountRepository.updateSuccessfulLogin(username, currentTime, ipAddress);
            if(userRoles.contains("ADMIN")) {
                emailService.sendAdminLoginEmail(account.getEmail(), account.getLogin(), ipAddress, account.getLanguage());
            }
            return jwtService.generatePair(account, userRoles);
        } else {
            accountRepository.updateFailedLogin(username, currentTime, ipAddress);
            throw new InvalidCredentialsException();
        }
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

    //@Retryable
    public void blockAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException());

        if (!account.isActive())
            throw new AccountNotActiveException(); //TODO zmienic

        //TODO obsluga wyjatku

        account.setActive(false);
        accountRepository.saveAndFlush(account);

//        emailService.sendBlockAccountEmail(account.getEmail(), account.getLogin(), account.getLanguage()); //TODO MAILE SIE ZMIENILY POPRAWIC!
        //TODO logowanie

    }

    public void unblockAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException());

        if (account.isActive())
            throw new AccountNotActiveException(); //TODO zmienic

        account.setActive(true);
        accountRepository.saveAndFlush(account);

    }

    public String me() {
        return "Hello " + SecurityContextHolder.getContext().getAuthentication().getName();
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    //TODO tu raczej bez powtarzania ale do ustalenia
    public void sendResetPasswordEmail(String email) {
        Account account = accountRepository.findByEmail(email);
        if(account == null) {
            throw new AccountNotFoundException();
        }
        String token = UUID.randomUUID().toString();
        passwordResetTokenService.createPasswordResetToken(account, token);
//        emailService.sendResetPasswordEmail(account.getEmail(), account.getLogin(), account.getLanguage(), token); //TODO MAILE SIE ZMIENILY POPRAWIC!
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void resetPassword(String token, ResetPasswordDTO resetPasswordDTO) {
        passwordResetTokenService.validatePasswordResetToken(token);
        Account account = accountRepository.findByEmail(resetPasswordDTO.email());
        if(account == null) {
            throw new AccountNotFoundException();
        }
        accountRepository.updatePassword(account.getLogin(), BCrypt.hashpw(resetPasswordDTO.password(), BCrypt.gensalt()));
    }

    public List<AccountDTO> getAllAccounts(Boolean active, Boolean verified) {
        List<Account> accounts = accountRepository.findByActiveAndVerified(active, verified);

        return accounts.stream()
                .map(accountMapper::toAccountDTO)
                .collect(Collectors.toList());
    }

    public void changeOwnEmail(String newEmail) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        handleEmailChange(account, newEmail);
    }

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

    public void resendEmailChangeLink() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        TokenEntity tokenEntity = tokenRepository.findByAccount(account).stream()
                .filter(jwt -> {
                    try {
                        String type = jwtTokenProvider.getType(jwt.getToken());
                        return "EMAIL_CHANGE".equals(type) && jwt.getExpiration().after(new Date());
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst().orElseThrow(EmailChangeTokenNotFoundException::new);

        String emailChangeToken = tokenEntity.getToken();
        String newEmail = jwtTokenProvider.getNewEmailFromToken(emailChangeToken);
        String confirmationURL = confirmURL + emailChangeToken;

        emailService.sendChangeEmail(account.getLogin(), newEmail, confirmationURL, account.getLanguage());
    }

    public AccountWithTokenDTO getAccountByLogin(String login) {
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        List<AccountRolesProjection> roles = accountRepository.findAccountRolesByLogin(login);

        AccountReadDTO dto = accountMapper.toReadDTO(account);
        String token = lockTokenService.generateToken(account.getId(), account.getVersion());

        return new AccountWithTokenDTO(dto, token, roles);
    }

    public AccountWithTokenDTO getAccountById(String id) {
        Account account = accountRepository.findById(UUID.fromString(id))
                .orElseThrow(AccountNotFoundException::new);

        AccountReadDTO dto = accountMapper.toReadDTO(account);
        String token = lockTokenService.generateToken(account.getId(), account.getVersion());
        List<AccountRolesProjection> roles = accountRepository.findAccountRolesByLogin(account.getLogin());

        return new AccountWithTokenDTO(dto, token, roles);
    }

    public void updateAccountById(String id, UpdateAccountDTO dto) {
        updateAccount(() -> accountRepository.findById(UUID.fromString(id))
                .orElseThrow(AccountNotFoundException::new), dto);
    }

    private void updateAccount(Supplier<Account> accountSupplier, UpdateAccountDTO dto) {
        LockTokenService.Record<UUID, Long> record = lockTokenService.verifyToken(dto.lockToken());

        Account account = accountSupplier.get();

        if (!account.isActive()) {
            throw new AccountNotActiveException();
        }

        if (!account.getId().equals(record.id())) {
            throw new InvalidLockTokenException();
        }

        if (!Objects.equals(account.getVersion(), record.version())) {
            throw new OptimisticLockException("Version mismatch");
        }

        account.setFirstName(dto.firstName());
        account.setLastName(dto.lastName());

        accountRepository.saveAndFlush(account);
    }

    public void updateMyAccount(String login, UpdateAccountDTO updateAccountDTO) {
        LockTokenService.Record<UUID, Long> record = lockTokenService.verifyToken(updateAccountDTO.lockToken());

        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);

        if (!account.isActive()) {
            throw new AccountNotActiveException();
        }

        if (!account.getId().equals(record.id())) {
            throw new InvalidLockTokenException();
        }

        account.setFirstName(updateAccountDTO.firstName());
        account.setLastName(updateAccountDTO.lastName());

        if (!Objects.equals(account.getVersion(), record.version())) {
            throw new OptimisticLockException("Version mismatch"); // TODO: need to catch this error because its runtime
        }

        accountRepository.saveAndFlush(account);
    }

    @PreAuthorize("permitAll()")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public void verifyAccount(String token){
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

    public boolean assignAdminRole(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        boolean hasActiveAdminRole = accountRepository.findAccountRolesByLogin(account.getLogin()).stream()
                .anyMatch(role -> role.getRoleName().equals("ADMIN") && role.isActive());

        if (!hasActiveAdminRole) {
            Admin admin = new Admin();
            admin.setAccount(account);
            admin.setActive(true);
            userRoleRepository.save(admin);
            return true;
        }
        return false;
    }

    public boolean assignDieticianRole(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        boolean hasActiveDieticianRole = accountRepository.findAccountRolesByLogin(account.getLogin()).stream()
                .anyMatch(role -> role.getRoleName().equals("DIETICIAN") && role.isActive());

        if (!hasActiveDieticianRole) {
            Dietician dietician = new Dietician();
            dietician.setAccount(account);
            dietician.setActive(true);
            userRoleRepository.save(dietician);
            return true;
        }
        return false;
    }

    public boolean assignClientRole(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        boolean hasActiveClientRole = accountRepository.findAccountRolesByLogin(account.getLogin()).stream()
                .anyMatch(role -> role.getRoleName().equals("CLIENT") && role.isActive());

        if (!hasActiveClientRole) {
            Client client = new Client();
            client.setAccount(account);
            client.setActive(true);
            userRoleRepository.save(client);
            return true;
        }
        return false;
    }

    public boolean revokeAdminRole(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        List<Admin> admins = userRoleRepository.findAdminsByAccount(account);

        Admin activeAdmin = admins.stream()
                .filter(Admin::isActive)
                .findFirst()
                .orElse(null);

        if (activeAdmin == null) {
            return false;
        }

        activeAdmin.setActive(false);
        userRoleRepository.save(activeAdmin);
        return true;
    }

    public boolean revokeDieticianRole(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        List<Dietician> dieticians = userRoleRepository.findDieticiansByAccount(account);

        Dietician activeDietician = dieticians.stream()
                .filter(Dietician::isActive)
                .findFirst()
                .orElse(null);

        if (activeDietician == null) {
            return false;
        }

        activeDietician.setActive(false);
        userRoleRepository.save(activeDietician);
        return true;
    }

    public boolean revokeClientRole(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        List<Client> clients = userRoleRepository.findClientsByAccount(account);

        Client activeClient = clients.stream()
                .filter(Client::isActive)
                .findFirst()
                .orElse(null);

        if (activeClient == null) {
            return false;
        }

        activeClient.setActive(false);
        userRoleRepository.save(activeClient);
        return true;
    }
}
