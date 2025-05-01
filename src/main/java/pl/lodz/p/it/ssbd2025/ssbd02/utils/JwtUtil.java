package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtUtil {
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();


    public boolean checkToken(String token) {
        return blacklistedTokens.contains(token);
    }

    public void invalidateToken(String token) {
        blacklistedTokens.add(token);
    }
}
