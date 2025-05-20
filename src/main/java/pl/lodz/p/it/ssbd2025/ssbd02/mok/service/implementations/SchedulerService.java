package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.TokenEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.ISchedulerService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@TransactionLogged
@Service
@RequiredArgsConstructor
public class SchedulerService implements ISchedulerService {

    private final EmailService emailService;

    private final TokenRepository tokenRepository;

    private final AccountRepository accountRepository;

    @Value("${account.verification.threshold}")
    private long accountVerificationThreshold;

    @Value("${mail.verify.url}")
    private String verificationURL;


    @Scheduled(fixedRateString = "${scheduler.deleteUnverifiedAccounts.fixedRate}")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public void deleteNotVerifiedAccounts() {
        Date date = new Date();
        List<TokenEntity> verificationTokens = new ArrayList<TokenEntity>();
        verificationTokens = tokenRepository.findAllByType(TokenType.VERIFICATION);
        if(!verificationTokens.isEmpty()) {
            for (TokenEntity token : verificationTokens) {
                if (token.getExpiration().before(date)) {
                    tokenRepository.delete(token);
                    if (!token.getAccount().isVerified()) {
                        accountRepository.delete(token.getAccount());
                        emailService.sendAccountDeletedEmail(token.getAccount().getEmail(), token.getAccount().getLogin(), token.getAccount().getLanguage());
                    }
                }
            }
        }
    }

    @Scheduled(fixedRateString = "${scheduler.remindUnverifiedAccounts.fixedRate}")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public void remindUnverifiedAccounts() {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.HOUR, (int)accountVerificationThreshold / 2);
        Date date = calendar.getTime();

        List<TokenEntity> tokensToRemind = new ArrayList<TokenEntity>();
        tokensToRemind = tokenRepository.findAllByType(TokenType.VERIFICATION);

        if(!tokensToRemind.isEmpty()) {
            for (TokenEntity token : tokensToRemind) {
                if (token.getExpiration().before(date) && !token.getAccount().isReminded()) {
                    token.getAccount().setReminded(true);
                    accountRepository.saveAndFlush(token.getAccount());
                    emailService.sendVerificationReminderEmail(token.getAccount().getEmail(), token.getAccount().getLogin(), verificationURL, token.getAccount().getLanguage(), new SensitiveDTO(token.getToken()));
                }
            }
        }
    }

    @Scheduled(fixedRateString = "${scheduler.unlock.accounts}")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public void unlockAccounts() {
        Date currentDate = new Date();
        List<Account> accounts = accountRepository.findByHasLockedUntil();
        if(!accounts.isEmpty()) {
            for (Account account : accounts) {
                if(account.getLockedUntil().before(currentDate)) {
                    account.setLockedUntil(null);
                    account.setActive(true);
                    accountRepository.saveAndFlush(account);
                }
            }
        }
    }

    @Scheduled(fixedRateString = "${scheduler.deleteUnverifiedAccounts.fixedRate}")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public void deleteExpiredTokens() {
        Date currentDate = new Date();
        tokenRepository.deleteTokenEntitiesExceptVerification(currentDate);
    }
}
