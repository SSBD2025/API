package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.TokenEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Component("MOKTokenRepository")
@Transactional(propagation = Propagation.MANDATORY)
public interface TokenRepository extends JpaRepository<TokenEntity, UUID>, AbstractRepository<TokenEntity> {
    List<TokenEntity> findByAccount(Account account);

    @PreAuthorize("permitAll()")
    @Modifying
    @Query("""
        delete TokenEntity t where t.account=:account and t.type=:type
    """)
    void deleteAllByAccountWithType(Account account, TokenType type);

    @Query("""
        select count(t) > 0 from TokenEntity t where t.account = :account and t.type = :type
    """)
    boolean existsByAccountWithType(Account account, TokenType type);

    @Query("""
        select t from TokenEntity t where t.account=:account and t.type=:type
    """)
    List<TokenEntity> findAllByAccountWithType(Account account, TokenType type);

    boolean existsByToken(String token);

//    @Query("SELECT j FROM TokenEntity j WHERE j.token = :token")
//    TokenEntity findByTokenValue(String token);

    @PreAuthorize("permitAll()")
    Optional<TokenEntity> findByToken(String token);

    @Override
    TokenEntity saveAndFlush(TokenEntity entity);

    TokenEntity findByType(TokenType type);

    List<TokenEntity> findByExpirationBefore(Date date);
}
