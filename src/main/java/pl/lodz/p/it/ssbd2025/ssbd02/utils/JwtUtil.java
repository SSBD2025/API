package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtUtil {
    public boolean checkPassword(String plaintext, String hash) {
        return BCrypt.checkpw(plaintext, hash);
    }
}
