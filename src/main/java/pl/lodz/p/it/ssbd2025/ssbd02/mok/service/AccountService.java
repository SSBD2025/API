package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import jakarta.persistence.OptimisticLockException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
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
import pl.lodz.p.it.ssbd2025.ssbd02.entities.JwtEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.UserRole;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.VerificationToken;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.*;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.InvalidCredentialsException;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.JwtRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.VerificationTokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.*;

import java.security.SecureRandom;

import java.util.*;

import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtUtil;

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
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
public class AccountService {

    @NotNull
    private final AccountRepository accountRepository;
    @NotNull
    private final JwtUtil jwtUtil;
    @NotNull
    private final JwtTokenProvider jwtTokenProvider;
    @NotNull
    private final EmailService emailService;
    @NotNull
    private final JwtService jwtService;
    @NotNull
    private final PasswordResetTokenService passwordResetTokenService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final AccountMapper accountMapper;
    private final LockTokenService lockTokenService;
    private final JwtRepository jwtRepository;
    @Value("${mail.confirmation.url}")
    private String confirmURL;
    @Value("${mail.revert.url}")
    private String revertURL;

//    public UserDetails loadUserByUsername(String username) {
//        Account account = accountRepository.findByLogin(username);
//        if(account == null) {
//            throw new AccountNotFoundException();
//        } else {
//            return account;
//        }
//    }

    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByLogin(login);
        if(account == null) {
            throw new AccountNotFoundException();
        }
        if(!BCrypt.checkpw(changePasswordDTO.oldPassword(), account.getPassword())) {
            throw new InvalidCredentialsException();
        }
        accountRepository.updatePassword(account.getLogin(), BCrypt.hashpw(changePasswordDTO.newPassword(), BCrypt.gensalt()));
    }

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
        int length = 12;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        return random.ints(length, 0, chars.length())
                .mapToObj(i -> String.valueOf(chars.charAt(i)))
                .collect(Collectors.joining());
    }

    public TokenPairDTO login(String username, String password, String ipAddress) { //todo 90% sure its not correct
        Account account = accountRepository.findByLogin(username);
        List<AccountRolesProjection> roles = accountRepository.findAccountRolesByLogin(username);
        List<String> userRoles = new ArrayList<>();
        roles.forEach(role -> {
            if (role.isActive()) { //todo this needs to be fixed later to support dynamic role granting
                userRoles.add(role.getRoleName());
            }
        });
        if (account == null) {
            throw new AccountNotFoundException();
        }
//        if (!account.isActive()) {
//            throw new AccountNotActiveException();
//        } //todo uncomment later
//        if (!account.isVerified()) {
//            throw new AccountNotVerifiedException();
//        }
        Date currentTime = new Date(System.currentTimeMillis());
        if (jwtUtil.checkPassword(password, account.getPassword())) {
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

    public void logout() {
        Account account = accountRepository.findByLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        jwtRepository.deleteAllByAccount(account);
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

        emailService.sendBlockAccountEmail(account.getEmail(), account.getLogin(), account.getLanguage());
        //TODO logowanie

    }

    public void sendResetPasswordEmail(String email) {
        Account account = accountRepository.findByEmail(email);
        if(account == null) {
            throw new AccountNotFoundException();
        }
        String token = UUID.randomUUID().toString();
        passwordResetTokenService.createPasswordResetToken(account, token);
        emailService.sendResetPasswordEmail(account.getEmail(), account.getLogin(), account.getLanguage(), token);
    }

    public void resetPassword(String token, ResetPasswordDTO resetPasswordDTO) {
        if(Objects.equals(passwordResetTokenService.validatePasswordResetToken(token), "Valid token")) {
            Account account = accountRepository.findByEmail(resetPasswordDTO.email());
            if(account == null) {
                throw new AccountNotFoundException();
            }
            accountRepository.updatePassword(account.getLogin(), BCrypt.hashpw(resetPasswordDTO.password(), BCrypt.gensalt()));
        }
        else if(Objects.equals(passwordResetTokenService.validatePasswordResetToken(token), "Invalid verification token")) {
            throw new InvalidCredentialsException();
        }
        else if(Objects.equals(passwordResetTokenService.validatePasswordResetToken(token), "Token expired")) {
            throw new TokenExpiredException();
        }
    }

    public List<AccountDTO> getAllAccounts(Boolean active, Boolean verified) {
        List<Account> accounts = accountRepository.findByActiveAndVerified(active, verified);

        return accounts.stream()
                .map(accountMapper::toAccountDTO)
                .collect(Collectors.toList());
    }

    public void changeEmail(String newEmail) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        Account account = accountRepository.findByLogin(login);
        if (account.getEmail().equals(newEmail)) {
            throw new AccountSameEmailException();
        }
        if (accountRepository.findByEmail(newEmail) != null) {
            throw new AccountEmailAlreadyInUseException();
        }

        String emailChangeToken = jwtTokenProvider.generateEmailChangeToken(account, newEmail);
        String confirmationURL = confirmURL + emailChangeToken;

        JwtEntity jwt = new JwtEntity(emailChangeToken, jwtTokenProvider.getExpiration(emailChangeToken), account);
        jwtRepository.save(jwt);

        emailService.sendChangeEmail(account.getLogin(), newEmail, confirmationURL, account.getLanguage());
    }

    public void confirmEmail(String token) {
        JwtEntity jwt = jwtRepository.findByTokenValue(token);
        if (jwt.getExpiration().before(new Date())) {
            jwtRepository.delete(jwt);
            throw new TokenExpiredException();
        }

        String newEmail = jwtTokenProvider.getNewEmailFromToken(token);
        UUID accountId = jwtTokenProvider.getAccountIdFromToken(token);

        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);

        String oldEmail = account.getEmail();

        account.setEmail(newEmail);
        accountRepository.saveAndFlush(account);
        jwtRepository.delete(jwt);

        String revertToken = jwtTokenProvider.generateEmailRevertToken(account, oldEmail);
        String revertChangeURL = revertURL + revertToken;

        JwtEntity jwtEntity = new JwtEntity(revertToken, jwtTokenProvider.getExpiration(revertToken), account);
        jwtRepository.save(jwtEntity);

        emailService.sendRevertChangeEmail(account.getLogin(), oldEmail, revertChangeURL, account.getLanguage());
    }

    public void revertEmailChange(String token) {
        JwtEntity jwt = jwtRepository.findByTokenValue(token);
        if (jwt.getExpiration().before(new Date())) {
            jwtRepository.delete(jwt);
            throw new TokenExpiredException();
        }

        String oldEmail = jwtTokenProvider.getOldEmailFromToken(token);
        UUID accountId = jwtTokenProvider.getAccountIdFromToken(token);

        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);

        account.setEmail(oldEmail);
        accountRepository.saveAndFlush(account);
        jwtRepository.delete(jwt);
    }

    public void resendEmailChangeLink() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        Account account = accountRepository.findByLogin(login);
        JwtEntity jwtEntity = jwtRepository.findByAccount(account).stream()
                .filter(jwt -> {
                    try {
                        String type = jwtTokenProvider.getType(jwt.getToken());
                        return "EMAIL_CHANGE".equals(type) && jwt.getExpiration().after(new Date());
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst().orElseThrow(EmailChangeTokenNotFoundException::new);

        String emailChangeToken = jwtEntity.getToken();
        String newEmail = jwtTokenProvider.getNewEmailFromToken(emailChangeToken);
        String confirmationURL = confirmURL + emailChangeToken;

        emailService.sendChangeEmail(account.getLogin(), newEmail, confirmationURL, account.getLanguage());
    }

    public AccountWithTokenDTO getAccountByLogin(String login) {
        Account account = accountRepository.findByLogin(login);

        if(account == null) {
            throw new AccountNotFoundException();
        }
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

        Account account = accountRepository.findByLogin(login);
        if (account == null) {
            throw new AccountNotFoundException();
        }

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

    public void verifyAccount(String token){
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElseThrow(TokenNotFoundException::new);
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
        verificationTokenRepository.delete(verificationToken);
        account.setVerified(true);
    }
}
