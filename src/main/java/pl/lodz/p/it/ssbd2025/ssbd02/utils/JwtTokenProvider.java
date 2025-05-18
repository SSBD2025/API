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
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token.*;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

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

    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false, transactionManager = "mokTransactionManager")
    public String generateAccessToken(Account account, List<String> roles) {
        return Jwts.builder()
                .subject(account.getLogin())
                .claim("roles", roles)
                .claim("typ", "access")
                .id(account.getId().toString())
                .subject(account.getLogin())
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getPrivateKey())
                .compact();
    }

    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false, transactionManager = "mokTransactionManager")
    public String generateRefreshToken(Account account) {
        return Jwts.builder()
                .id(account.getId().toString())
                .subject(account.getLogin())
                .claim("typ", "refresh")
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getPrivateKey())
                .compact();
    }

    public String generateAccess2FAToken(Account account) {
        return Jwts.builder()
                .id(account.getId().toString())
                .subject(account.getLogin())
                .claim("typ", "access2fa")
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + access2faExpiration))
                .signWith(getPrivateKey())
                .compact();
    }

    public String getLogin(String token) {
        return Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public List<String> getRoles(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("roles", List.class);
    }

    public String getIssuer(String token) {
        return Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getIssuer();
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false, transactionManager = "mokTransactionManager")
    public Date getExpiration(String token) {
        return Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }

    @PreAuthorize("permitAll()")
    public String getType(String token) {
        return (String) Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("typ");
    }

    private RSAPrivateKey getPrivateKey() {
        try (InputStream is = new ClassPathResource("keys/private_key.pem").getInputStream()) {
            String key = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load private key", e);
        }
    }

    public RSAPublicKey getPublicKey() {
        try (InputStream is = new ClassPathResource("keys/public_key.pem").getInputStream()) {
            String key = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load public key", e);
        }
    }

    public void validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getPublicKey()).build().parseSignedClaims(token);
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

    public String generateEmailChangeToken(Account account, String newEmail) {
        return Jwts.builder()
                .subject(account.getLogin())
                .id(account.getId().toString())
                .claim("newEmail", newEmail)
                .claim("typ", "EMAIL_CHANGE")
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + emailChangeExpiration))
                .signWith(getPrivateKey())
                .compact();
    }

    public String generateEmailRevertToken(Account account, String oldEmail) {
        return Jwts.builder()
                .subject(account.getLogin())
                .id(account.getId().toString())
                .claim("oldEmail", oldEmail)
                .claim("typ", "EMAIL_REVERT")
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + emailChangeExpiration))
                .signWith(getPrivateKey())
                .compact();
    }

    public String getNewEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("newEmail", String.class);
    }

    public String getOldEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("oldEmail", String.class);
    }

    public UUID getAccountIdFromToken(String token) {
        String id = Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getId();

        return UUID.fromString(id);
    }

    public void cookieSetter(String refresh, int jwtRefreshExpiration, HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", refresh);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(jwtRefreshExpiration/1000);
        response.addCookie(cookie);
    }
}
