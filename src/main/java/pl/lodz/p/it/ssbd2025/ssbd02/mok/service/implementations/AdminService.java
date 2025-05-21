package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AdminDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.AdminMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Admin;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ChangePasswordEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.TokenEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AccountConstraintViolationException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AdminRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.ChangePasswordRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IAdminService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.TokenUtil;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Service
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.REQUIRES_NEW,  readOnly = true, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
public class AdminService implements IAdminService {

    private final AdminRepository adminRepository;
    private final AccountRepository accountRepository;
    private final AdminMapper adminMapper;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;
    private final TokenUtil tokenUtil;
    private final AccountService accountService;
    private final ChangePasswordRepository changePasswordRepository;

    @Value("${mail.verify.url}")
    private String verificationURL;

    @Value("${account.verification.threshold}")
    private long accountVerificationThreshold;

    @PreAuthorize("hasRole('ADMIN')") //ostatecznie to odkomentowac
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public Admin createAdmin(Admin newAdmin, Account newAccount) {
        if(!isLoginUnique(newAccount.getLogin())) {
            throw new AccountConstraintViolationException(ExceptionConsts.ACCOUNT_CONSTRAINT_VIOLATION + ": login already in use");
        } else if(!isEmailUnique(newAccount.getEmail())) {
            throw new AccountConstraintViolationException(ExceptionConsts.ACCOUNT_CONSTRAINT_VIOLATION + ": email already in use");
        }
        newAccount.setPassword(BCrypt.hashpw(newAccount.getPassword(), BCrypt.gensalt()));
        newAdmin.setAccount(newAccount);
        newAccount.getUserRoles().add(newAdmin);
        Account createdAccount = accountRepository.saveAndFlush(newAccount);
        ChangePasswordEntity changePasswordEntity = new ChangePasswordEntity(createdAccount, true);
        changePasswordRepository.saveAndFlush(changePasswordEntity);
        String token = UUID.randomUUID().toString();
        emailService.sendActivationMail(newAccount.getEmail(), newAccount.getLogin(), verificationURL, newAccount.getLanguage(), new SensitiveDTO(token));
        tokenRepository.saveAndFlush(new TokenEntity(token, tokenUtil.generateHourExpiration(accountVerificationThreshold), createdAccount, TokenType.VERIFICATION));
        return adminRepository.saveAndFlush(newAdmin);
    }

    @Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager", readOnly = true)
    @PreAuthorize("permitAll()")
    public boolean isLoginUnique(String login) {
        return accountRepository.findByLogin(login).isEmpty();
    }

    @Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager", readOnly = true)
    @PreAuthorize("permitAll()")
    public boolean isEmailUnique(String email) {
        return accountRepository.findByEmail(email).isEmpty();
    }
}
