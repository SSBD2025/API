package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.keycloak.Token;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountRolesProjection;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.TokenPairDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.JwtEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.JwtRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
public class JwtService {
    @NotNull
    private final JwtRepository jwtRepository;

    @NotNull
    private final JwtTokenProvider jwtTokenProvider;

    @NotNull
    private final AccountRepository accountRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    public TokenPairDTO generatePair(@NotNull Account account, @NotNull List<String> roles) {// no clue if this method is correct
        String accessToken = jwtTokenProvider.generateAccessToken(account, roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(account);
        Date accessExpiration = jwtTokenProvider.getExpiration(accessToken);
        Date refreshExpiration = jwtTokenProvider.getExpiration(refreshToken);
        jwtRepository.saveAndFlush(new JwtEntity(accessToken, accessExpiration, account));
        jwtRepository.saveAndFlush(new JwtEntity(refreshToken, refreshExpiration, account));
        return new TokenPairDTO(accessToken, refreshToken);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    public TokenPairDTO refresh(String token) { //takes refresh token, produces access token and saves it into the database
        if(!jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("todo");
        } else if(!check(token)) {
            throw new RuntimeException("todo2");
        } else if(!jwtTokenProvider.getType(token).equals("refresh")) {
            throw new RuntimeException("todo3");
        }
        Account account = accountRepository.findByLogin(jwtTokenProvider.getSubject(token));
        List<AccountRolesProjection> roles = accountRepository.findAccountRolesByLogin(account.getLogin());
        List<String> userRoles = new ArrayList<>();
        roles.forEach(role -> {
            if (role.isActive()) { //todo this needs to be fixed later to support dynamic role granting
                userRoles.add(role.getRoleName());
            }
        });
        String newAccessToken = jwtTokenProvider.generateAccessToken(account, userRoles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(account);
        Date expiration = jwtTokenProvider.getExpiration(token);
        jwtRepository.deleteAllByAccount(account);
        jwtRepository.saveAndFlush(new JwtEntity(newAccessToken, expiration, account));
        jwtRepository.saveAndFlush(new JwtEntity(newRefreshToken, expiration, account));
        return new TokenPairDTO(newAccessToken, newRefreshToken);
    }

    public List<JwtEntity> findByAccount(Account account) {
        return jwtRepository.findByAccount(account);
    }

    public boolean check(String token) { //checks if token is actually in database
        return jwtRepository.existsByToken(token);
    }
}
