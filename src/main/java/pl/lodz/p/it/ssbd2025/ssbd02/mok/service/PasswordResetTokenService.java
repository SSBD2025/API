package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.PasswordResetToken;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.PasswordResetTokenRepository;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

//@Component
@RequiredArgsConstructor
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public void createPasswordResetToken(Account account, String token) {
        if(passwordResetTokenRepository.findByAccount(account) != null) {
           passwordResetTokenRepository.deleteByAccount(account);
        }
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, account);
        passwordResetTokenRepository.saveAndFlush(passwordResetToken);
    }

    public String validatePasswordResetToken(String passwordResetToken) {
        Calendar calendar = Calendar.getInstance();
        PasswordResetToken passwordToken = passwordResetTokenRepository.findByToken(passwordResetToken);
        if(passwordToken == null){
            return "Invalid verification token";
        }
        passwordResetTokenRepository.delete(passwordToken);
        Account account = passwordToken.getAccount();
        if ((passwordToken.getExpiration().getTime()-calendar.getTime().getTime())<= 0){
            return "Token expired";
        }
        return "Valid token";
    }


}

