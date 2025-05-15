package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.TokenEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;

import java.security.SecureRandom;
import java.util.Date;

@MethodCallLogged
@Component
@RequiredArgsConstructor
public class TokenUtil {
    private final TokenRepository tokenRepository;

    public boolean checkPassword(String passwordPlaintext, String passwordHash) {
        return BCrypt.checkpw(passwordPlaintext, passwordHash);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public String generateTwoFactorCode(Account account) {
        SecureRandom random = new SecureRandom();
        random.setSeed(System.currentTimeMillis());
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            int digit = random.nextInt(10);
            sb.append(digit);
        }
        String code = sb.toString();
        String hashedCode = BCrypt.hashpw(code, BCrypt.gensalt());
        tokenRepository.deleteAllByAccountWithType(account, TokenType.TWO_FACTOR);
        tokenRepository.saveAndFlush(new TokenEntity(hashedCode, generateMinuteExpiration(5), account, TokenType.TWO_FACTOR));
        return code;
    }


    public Date generateMillisecondExpiration(long value){
        return new Date(new Date().getTime() + value);
    }

    public Date generateSecondExpiration(long value){
        return new Date(new Date().getTime() + value * 1000L);
    }

    public Date generateMinuteExpiration(long value){
        return new Date(new Date().getTime() + value * 60000L);
    }

    public Date generateHourExpiration(long value){
        return new Date(new Date().getTime() + value * 3600000L);
    }

    public Date generateDayExpiration(long value){
        return new Date(new Date().getTime() + value * 86400000L);
    }
}
