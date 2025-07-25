package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
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

    @Value("${app.jwt_2fa_expiration}")
    private int twoFactorExpiration;

    @PreAuthorize("permitAll()")
    public boolean checkPassword(SensitiveDTO passwordPlaintext, String passwordHash) {
        return BCrypt.checkpw(passwordPlaintext.getValue(), passwordHash);
    }

    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public SensitiveDTO generateTwoFactorCode(Account account) {
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
        tokenRepository.saveAndFlush(new TokenEntity(hashedCode, generateMinuteExpiration(twoFactorExpiration), account, TokenType.TWO_FACTOR));
        return new SensitiveDTO(code);
    }

    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    public SensitiveDTO generateAutoLockUnlockCode(Account account) {
        SecureRandom random = new SecureRandom();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        String code = sb.toString();
        tokenRepository.deleteAllByAccountWithType(account, TokenType.UNLOCK_CODE);
        tokenRepository.saveAndFlush(new TokenEntity(code, generateMinuteExpiration(twoFactorExpiration), account, TokenType.UNLOCK_CODE));
        return new SensitiveDTO(code);
    }

    @PreAuthorize("permitAll()")
    public Date generateMillisecondExpiration(long value){
        return new Date(new Date().getTime() + value);
    }

    @PreAuthorize("permitAll()")
    public Date generateSecondExpiration(long value){
        return new Date(new Date().getTime() + value * 1000L);
    }

    @PreAuthorize("permitAll()")
    public Date generateMinuteExpiration(long value){
        return new Date(new Date().getTime() + value * 60000L);
    }

    @PreAuthorize("permitAll()")
    public Date generateHourExpiration(long value){
        return new Date(new Date().getTime() + value * 3600000L);
    }

    @PreAuthorize("permitAll()")
    public Date generateDayExpiration(long value){
        return new Date(new Date().getTime() + value * 86400000L);
    }
}
