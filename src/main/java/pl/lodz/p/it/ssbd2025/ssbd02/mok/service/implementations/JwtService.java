package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountRolesProjection;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.TokenPairDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.TokenEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.*;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IJwtService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.TokenUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Service
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
public class JwtService implements IJwtService {
    @NotNull
    private final TokenRepository tokenRepository;

    @NotNull
    private final JwtTokenProvider jwtTokenProvider;

    @NotNull
    private final AccountRepository accountRepository;

    @Value("${app.jwt_refresh_expiration}")
    private int jwtRefreshExpiration;

    private final TokenUtil tokenUtil;

    @PreAuthorize("permitAll()")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class,}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public TokenPairDTO generatePair(@NotNull Account account, @NotNull List<String> roles) {
        String accessToken = jwtTokenProvider.generateAccessToken(account, roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(account);
        Date accessExpiration = jwtTokenProvider.getExpiration(accessToken);
        Date refreshExpiration = jwtTokenProvider.getExpiration(refreshToken);
        tokenRepository.saveAndFlush(new TokenEntity(accessToken, accessExpiration, account, TokenType.ACCESS));
        tokenRepository.saveAndFlush(new TokenEntity(refreshToken, refreshExpiration, account, TokenType.REFRESH));
        return new TokenPairDTO(accessToken, refreshToken, account.isTwoFactorAuth());
    }

    @PreAuthorize("permitAll()")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class,
            TokenNotFoundException.class, //chyba teoretycznie moze zostac wygenerowany wspolbieznie przy logowaniu i refreshu jednoczesnie
            AccountHasNoRolesException.class //admin moze je dodac/aktywowac wspolbieznie
    }, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public SensitiveDTO refresh(HttpServletRequest request, HttpServletResponse response) { //takes refresh token, produces access token and saves it into the database
        String token = "";
        if(request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }
        if (Objects.equals(token, "")){
            throw new CookieNotFoundException();
        }
        if(!jwtTokenProvider.validateToken(token)) {
            throw new TokenSignatureInvalidException();
        } else if(!tokenRepository.existsByToken(token)) {
            throw new TokenNotFoundException();
        } else if(!jwtTokenProvider.getType(token).equals("refresh")) {
            throw new TokenTypeInvalidException();
        }
        Account account = accountRepository.findByLogin(jwtTokenProvider.getSubject(token)).orElseThrow(AccountNotFoundException::new);
        List<AccountRolesProjection> roles = accountRepository.findAccountRolesByLogin(account.getLogin());
        List<String> userRoles = new ArrayList<>();
        if(roles.isEmpty()) {
            throw new AccountHasNoRolesException();
        }
        roles.forEach(role -> {
            if (role.isActive()) {
                userRoles.add(role.getRoleName());
            }
        });
        if(userRoles.isEmpty()) {
            throw new AccountHasNoRolesException();
        }
        String newAccessToken = jwtTokenProvider.generateAccessToken(account, userRoles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(account);

        jwtTokenProvider.cookieSetter(newAccessToken, jwtRefreshExpiration, response);

        Date expiration = jwtTokenProvider.getExpiration(token);
        tokenRepository.deleteAllByAccountWithType(account, TokenType.ACCESS);
        tokenRepository.deleteAllByAccountWithType(account, TokenType.REFRESH);
        tokenRepository.saveAndFlush(new TokenEntity(newAccessToken, expiration, account, TokenType.ACCESS));
        tokenRepository.saveAndFlush(new TokenEntity(newRefreshToken, expiration, account, TokenType.REFRESH));
        return new SensitiveDTO(newAccessToken);
    }

    public boolean check(String token) { //checks if token is actually in database
        return tokenRepository.existsByToken(token);
    }
}
