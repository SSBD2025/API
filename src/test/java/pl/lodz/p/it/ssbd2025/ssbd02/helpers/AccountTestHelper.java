package pl.lodz.p.it.ssbd2025.ssbd02.helpers;

import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;

@TestComponent
public class AccountTestHelper {

    @Autowired
    private AccountRepository accountRepository;

    //TODO THIS ABSOLUTELY CANNOT LEAK TO PROD
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void activateByLogin(String login) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalStateException("Account not found"));
        account.setActive(true);
        accountRepository.saveAndFlush(account);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void verifyByLogin(String login) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalStateException("Account not found"));
        account.setVerified(true);
        accountRepository.saveAndFlush(account);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void activateAndVerifyByLogin(String login) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalStateException("Account not found"));
        account.setActive(true);
        account.setVerified(true);
        accountRepository.saveAndFlush(account);
    }

    public static String extractTextFromMimeMessage(MimeMessage message) throws Exception {
        Object content = message.getContent();
        return extractText(content);
    }

    public static String extractText(Object content) throws Exception {
        if (content instanceof String) {
            return (String) content;
        } else if (content instanceof MimeMultipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);
                String result = extractText(part.getContent());
                if (result != null) return result;
            }
        }
        return null;
    }
}
