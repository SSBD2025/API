package pl.lodz.p.it.ssbd2025.ssbd02.helpers;

import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.UserRoleRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.TokenUtil;

import java.util.UUID;

@TestComponent
public class AccountTestHelper {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TokenUtil tokenUtil;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;

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

    @Transactional(readOnly = true, transactionManager = "mokTransactionManager")
    public Account getClientWithRolesById(UUID id) {
        return accountRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new IllegalStateException("Account not found"));
    }

    @Transactional(readOnly = true, transactionManager = "mokTransactionManager")
    public Account getClientWithRolesByLogin(String login) {
        return accountRepository.findByLoginWithRoles(login)
                .orElseThrow(() -> new IllegalStateException("Account not found"));
    }


    @Transactional(readOnly = true, transactionManager = "mokTransactionManager")
    public Account getClientByLogin(String login) {
        return accountRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalStateException("Account not found"));
    }

    @Transactional(readOnly = true, transactionManager = "mokTransactionManager")
    public Account getClientById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Account not found"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void checkPassword(String login, String password) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalStateException("Account not found"));
        if(!tokenUtil.checkPassword(password, account.getPassword())) {
            throw new IllegalStateException("Wrong password");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void setPassword(String login, String password) {
        accountRepository.updatePassword(login, BCrypt.hashpw(password, BCrypt.gensalt()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public String getToken(String token) {
        return tokenRepository.findByToken(token).orElseThrow().getToken();
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
