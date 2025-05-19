package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.ILockTokenService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@PropertySource("classpath:secrets.properties")
public class LockTokenService implements ILockTokenService {

    @Value("${app.optimistic-lock-secret}")
    private String secret;

    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')") //TODO sprawdzic
    public String generateToken(UUID id, Long version) {
        String payload = id + ":" + version;
        String signature = hmacSha256(payload, secret);
        return Base64.getEncoder().encodeToString((payload + ":" + signature).getBytes(StandardCharsets.UTF_8));
    }

    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')") //TODO sprawdzic
    public Record<UUID, Long> verifyToken(String token) {
        String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
        String[] parts = decoded.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid token");
        }

        UUID id = UUID.fromString(parts[0]);
        Long version = Long.parseLong(parts[1]);
        String signature = parts[2];

        String expected = hmacSha256(parts[0] + ":" + parts[1], secret);
        if (!expected.equals(signature)) {
            throw new SecurityException("Invalid signature");
        }

        return new Record<>(id, version);
    }

    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')") //TODO sprawdzic
    private String hmacSha256(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secretKey);
            return Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch(Exception e) {
            throw new RuntimeException("Failed to generate HMAC", e);
        }
    }

    public record Record<UUID, Long>(UUID id, Long version) {}
}
