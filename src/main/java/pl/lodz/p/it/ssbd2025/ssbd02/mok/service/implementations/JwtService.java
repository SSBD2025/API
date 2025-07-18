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
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token.TokenBaseException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token.TokenNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token.TokenSignatureInvalidException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token.TokenTypeInvalidException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IJwtService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.TokenUtil;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.JwtConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.TokenConsts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Service
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
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
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class,}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public TokenPairDTO generatePair(@NotNull Account account, @NotNull List<String> roles) {
        SensitiveDTO accessToken = jwtTokenProvider.generateAccessToken(account, roles);
        SensitiveDTO refreshToken = jwtTokenProvider.generateRefreshToken(account);
        Date accessExpiration = jwtTokenProvider.getExpiration(accessToken);
        Date refreshExpiration = jwtTokenProvider.getExpiration(refreshToken);
        tokenRepository.saveAndFlush(new TokenEntity(accessToken.getValue(), accessExpiration, account, TokenType.ACCESS));
        tokenRepository.saveAndFlush(new TokenEntity(refreshToken.getValue(), refreshExpiration, account, TokenType.REFRESH));
        return new TokenPairDTO(accessToken.getValue(), refreshToken.getValue(), account.isTwoFactorAuth());
    }

    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class,}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public SensitiveDTO generateAccess(@NotNull Account account, @NotNull List<String> roles) {
        SensitiveDTO accessToken = jwtTokenProvider.generateAccessToken(account, roles);
        Date accessExpiration = jwtTokenProvider.getExpiration(accessToken);
        tokenRepository.saveAndFlush(new TokenEntity(accessToken.getValue(), accessExpiration, account, TokenType.ACCESS));
        return accessToken;
    }

    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class,}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public SensitiveDTO generateShorterRefresh(@NotNull Account account, @NotNull List<String> roles) {
        SensitiveDTO refreshToken = jwtTokenProvider.generateShorterRefreshToken(account);
        Date refreshExpiration = jwtTokenProvider.getExpiration(refreshToken);
        tokenRepository.saveAndFlush(new TokenEntity(refreshToken.getValue(), refreshExpiration, account, TokenType.REFRESH));
        return refreshToken;
    }

    @PreAuthorize("permitAll()")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class,
            TokenNotFoundException.class,
            AccountHasNoRolesException.class
    }, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public SensitiveDTO refresh(HttpServletRequest request, HttpServletResponse response) {
        String token = "";
        if(request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (TokenConsts.REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }
        if (Objects.equals(token, "")){
            throw new CookieNotFoundException();
        }
        jwtTokenProvider.validateToken(new SensitiveDTO(token));
        if(!tokenRepository.existsByToken(token)) {
            throw new TokenNotFoundException();
        } else if(!jwtTokenProvider.getType(new SensitiveDTO(token)).equals(JwtConsts.TYPE_REFRESH)) {
            throw new TokenTypeInvalidException();
        }
        Account account = accountRepository.findByLogin(jwtTokenProvider.getSubject(new SensitiveDTO(token))).orElseThrow(AccountNotFoundException::new);
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
        SensitiveDTO newAccessToken = jwtTokenProvider.generateAccessToken(account, userRoles);
        SensitiveDTO newRefreshToken = jwtTokenProvider.generateRefreshToken(account);

        jwtTokenProvider.cookieSetter(new SensitiveDTO(newRefreshToken.getValue()), jwtRefreshExpiration, response);
        Date expiration = jwtTokenProvider.getExpiration(new SensitiveDTO(token));
        tokenRepository.deleteAllByAccountWithType(account, TokenType.ACCESS);
        tokenRepository.deleteAllByAccountWithType(account, TokenType.REFRESH);
        tokenRepository.saveAndFlush(new TokenEntity(newAccessToken.getValue(), expiration, account, TokenType.ACCESS));
        tokenRepository.saveAndFlush(new TokenEntity(newRefreshToken.getValue(), expiration, account, TokenType.REFRESH));
        return newAccessToken;
    }

    @PreAuthorize("permitAll()")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
    public boolean check(String token) { //checks if token is actually in database
        return tokenRepository.existsByToken(token);
    }
}
