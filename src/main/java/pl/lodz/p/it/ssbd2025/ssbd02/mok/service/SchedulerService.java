package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.TokenEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class SchedulerService {

    private final EmailService emailService;

    private final TokenRepository tokenRepository;

    private final AccountRepository accountRepository;

    @Scheduled(fixedRateString = "${scheduler.deleteUnverifiedAccounts.fixedRate}")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    public void deleteNotVerifiedAccounts() {
        Date date = new Date();
        List<TokenEntity> expiredTokens = new ArrayList<TokenEntity>();
        expiredTokens = tokenRepository.findByExpirationBefore(date);
        for (TokenEntity token : expiredTokens) {
                tokenRepository.delete(token);
                if (!token.getAccount().isVerified()) {
                    accountRepository.delete(token.getAccount());
                    emailService.sendAccountDeletedEmail(token.getAccount().getEmail(), token.getAccount().getLogin(), token.getAccount().getLanguage());
                }
        }

    }
}
