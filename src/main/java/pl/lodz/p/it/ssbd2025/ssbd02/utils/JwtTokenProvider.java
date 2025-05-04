package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.UserRoleDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.UserRole;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

@Component
@PropertySource("classpath:secrets.properties")
public class JwtTokenProvider {

    @Value("${app.jwt_secret}")
    private String secret;

    @Value("${app.jwt_expiration}")
    private long expiration;

    @Value("${app.jwt_issuer}")
    private String issuer;

    public String generateToken(Account account, List<String> roles) {

//        Collection<? extends GrantedAuthority> authorities = account.getAuthorities();
//        List<String> roleNames = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()); //neither this
//        account.getUserRoles().forEach(userRole -> roleNames.add(userRole.getRoleName())); //nor this works, as it pulls entire role by discriminator and causes permission error (dietary_restrictions etc)
        //List<String> roleNames = roles.stream().map(UserRole::getRoleName).collect(Collectors.toList());
//        userRoleDTOS.forEach(userRoleDTO -> roleNames.add(userRoleDTO.getRoleName()));


        return Jwts.builder()
                .subject(account.getLogin())
                .claim("roles", roles)
                .id(account.getId().toString())
                .subject(account.getLogin())
                .issuer(issuer) //is this needed? <- yes it is
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getPrivateKey())
                .compact();
    }

    public String getLogin(String token) {
        return Jwts.parser()
                .verifyWith(getPublicKey()) // use RSA public key
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
        String issuer = Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getIssuer();
        System.out.println("ISSUER: " + issuer);

        return issuer;
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


    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getPublicKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
