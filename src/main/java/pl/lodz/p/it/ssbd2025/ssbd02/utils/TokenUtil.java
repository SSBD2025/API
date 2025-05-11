package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.util.Date;

@MethodCallLogged
@Component
public class TokenUtil {
    public boolean checkPassword(String plaintext, String hash) {
        return BCrypt.checkpw(plaintext, hash);
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
