package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token.*;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.JwtConsts;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@MethodCallLogged
@Component
@PropertySource("classpath:secrets.properties")
public class JwtTokenProvider {

    @Value("${app.jwt_expiration}")
    private long expiration;

    @Value("${app.jwt_refresh_expiration}")
    private long refreshExpiration;

    @Value("${app.jwt_2fa_access_expiration}")
    private long access2faExpiration;

    @Value("${app.jwt_issuer}")
    private String issuer;

    @Value("${email.change_expiration}")
    private int emailChangeExpiration;

    @Value("${app.environment}")
    private String environment;

    @PreAuthorize("permitAll()")
    public SensitiveDTO generateAccessToken(Account account, List<String> roles) {
        return new SensitiveDTO(Jwts.builder()
                .subject(account.getLogin())
                .claim(JwtConsts.CLAIM_ROLES, roles)
                .claim(JwtConsts.CLAIM_TYPE, JwtConsts.TYPE_ACCESS)
                .id(account.getId().toString())
                .subject(account.getLogin())
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getPrivateKey())
                .compact());
    }

    @PreAuthorize("permitAll()")
    public SensitiveDTO generateRefreshToken(Account account) {
        return new SensitiveDTO(Jwts.builder()
                .id(account.getId().toString())
                .subject(account.getLogin())
                .claim(JwtConsts.CLAIM_TYPE, JwtConsts.TYPE_REFRESH)
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getPrivateKey())
                .compact());
    }

    @PreAuthorize("permitAll()")
    public SensitiveDTO generateShorterRefreshToken(Account account) {
        return new SensitiveDTO(Jwts.builder()
                .id(account.getId().toString())
                .subject(account.getLogin())
                .claim(JwtConsts.CLAIM_TYPE, JwtConsts.TYPE_REFRESH)
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + (refreshExpiration/2)))
                .signWith(getPrivateKey())
                .compact());
    }

    @PreAuthorize("permitAll()")
    public SensitiveDTO generateAccess2FAToken(Account account) {
        return new SensitiveDTO(Jwts.builder()
                .id(account.getId().toString())
                .subject(account.getLogin())
                .claim(JwtConsts.CLAIM_TYPE, JwtConsts.TYPE_ACCESS_2FA)
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + access2faExpiration))
                .signWith(getPrivateKey())
                .compact());
    }

    public String getLogin(SensitiveDTO token) {
        return Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token.getValue())
                .getPayload()
                .getSubject();
    }

    public List<String> getRoles(SensitiveDTO token) {
        Claims claims = Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token.getValue())
                .getPayload();
        return claims.get(JwtConsts.CLAIM_ROLES, List.class);
    }

    public String getIssuer(SensitiveDTO token) {
        return Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token.getValue())
                .getPayload()
                .getIssuer();
    }

    @PreAuthorize("permitAll()")
    public String getSubject(SensitiveDTO token) {
        return Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token.getValue())
                .getPayload()
                .getSubject();
    }

    @PreAuthorize("permitAll()")
    public Date getExpiration(SensitiveDTO token) {
        return Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token.getValue())
                .getPayload()
                .getExpiration();
    }

    @PreAuthorize("permitAll()")
    public String getType(SensitiveDTO token) {
        return (String) Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token.getValue())
                .getPayload()
                .get(JwtConsts.CLAIM_TYPE);
    }

    @PreAuthorize("permitAll()")
    private RSAPrivateKey getPrivateKey() {
        try (InputStream is = new ClassPathResource(JwtConsts.PRIVATE_KEY_PATH).getInputStream()) {
            String key = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replace(JwtConsts.BEGIN_PRIVATE_KEY, "")
                    .replace(JwtConsts.END_PRIVATE_KEY, "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance(JwtConsts.ALGORITHM);
            return (RSAPrivateKey) kf.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load private key", e);
        }
    }

    @PreAuthorize("permitAll()")
    public RSAPublicKey getPublicKey() {
        try (InputStream is = new ClassPathResource(JwtConsts.PUBLIC_KEY_PATH).getInputStream()) {
            String key = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replace(JwtConsts.BEGIN_PUBLIC_KEY, "")
                    .replace(JwtConsts.END_PUBLIC_KEY, "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance(JwtConsts.ALGORITHM);
            return (RSAPublicKey) kf.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load public key", e);
        }
    }

    @PreAuthorize("permitAll()")
    public void validateToken(SensitiveDTO token) {
        try {
            Jwts.parser().verifyWith(getPublicKey()).build().parseSignedClaims(token.getValue());
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException();
        } catch (SignatureException e) {
            throw new TokenSignatureInvalidException();
        } catch (MalformedJwtException e) {
            throw new TokenMalformedException();
        } catch (UnsupportedJwtException e) {
            throw new TokenUnsupportedException();
        } catch (IllegalArgumentException e) {
            throw new TokenInvalidException();
        }
    }

    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public SensitiveDTO generateEmailChangeToken(Account account, String newEmail) {
        return new SensitiveDTO(Jwts.builder()
                .subject(account.getLogin())
                .id(account.getId().toString())
                .claim(JwtConsts.CLAIM_NEW_EMAIL, newEmail)
                .claim(JwtConsts.CLAIM_TYPE, JwtConsts.TYPE_EMAIL_CHANGE)
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + emailChangeExpiration))
                .signWith(getPrivateKey())
                .compact());
    }

    @PreAuthorize("permitAll()")
    public SensitiveDTO generateEmailRevertToken(Account account, String oldEmail) {
        return new SensitiveDTO(Jwts.builder()
                .subject(account.getLogin())
                .id(account.getId().toString())
                .claim(JwtConsts.CLAIM_OLD_EMAIL, oldEmail)
                .claim(JwtConsts.CLAIM_TYPE, JwtConsts.TYPE_EMAIL_REVERT)
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + emailChangeExpiration))
                .signWith(getPrivateKey())
                .compact());
    }

    @PreAuthorize("permitAll()")
    public String getNewEmailFromToken(SensitiveDTO token) {
        Claims claims = Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token.getValue())
                .getPayload();

        return claims.get(JwtConsts.CLAIM_NEW_EMAIL, String.class);
    }

    @PreAuthorize("permitAll()")
    public String getOldEmailFromToken(SensitiveDTO token) {
        Claims claims = Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token.getValue())
                .getPayload();

        return claims.get(JwtConsts.CLAIM_OLD_EMAIL, String.class);
    }

    @PreAuthorize("permitAll()")
    public UUID getAccountIdFromToken(SensitiveDTO token) {
        String id = Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token.getValue())
                .getPayload()
                .getId();

        return UUID.fromString(id);
    }

    @PreAuthorize("permitAll()") //TODO sprawdzic
    public void cookieSetter(SensitiveDTO refresh, int jwtRefreshExpiration, HttpServletResponse response) {
        Cookie cookie = new Cookie(JwtConsts.REFRESH_TOKEN_COOKIE, refresh.getValue());
        cookie.setHttpOnly(true);
        cookie.setSecure(JwtConsts.ENVIRONMENT_PROD.equalsIgnoreCase(environment));
        cookie.setPath("/");
        cookie.setMaxAge(jwtRefreshExpiration/1000);
        response.addCookie(cookie);
    }

    @PreAuthorize("hasRole('ADMIN')||hasRole('DIETICIAN')||hasRole('CLIENT')")
    public void cookieClear(HttpServletResponse response) {
        Cookie cookie = new Cookie(JwtConsts.REFRESH_TOKEN_COOKIE, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(JwtConsts.ENVIRONMENT_PROD.equalsIgnoreCase(environment));
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
