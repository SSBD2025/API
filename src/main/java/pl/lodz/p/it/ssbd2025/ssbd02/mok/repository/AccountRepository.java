package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountRolesProjection;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
@Component("MOKAccountRepository")
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
public interface AccountRepository extends AbstractRepository<Account> {

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("permitAll()")
    Optional<Account> findByLogin(@NotBlank @Size(min = AccountConsts.LOGIN_MIN, max = AccountConsts.LOGIN_MAX) String login);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("permitAll()")
    Optional<Account> findByEmail(@Email String email);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("permitAll()")
    @Query("""
        select a.login as login, ur.roleName as roleName, ur.active as active
        from Account a join a.userRoles ur
        where a.login = :login
    """)
    List<AccountRolesProjection> findAccountRolesByLogin(@NotBlank @Size(min = AccountConsts.LOGIN_MIN, max = AccountConsts.LOGIN_MAX) String login);

    //Uses only in test scope
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Modifying
    @Query("UPDATE Account a SET a.password = :newPassword WHERE a.login = :login")
    void updatePassword(@Param("login") String login, @Param("newPassword") String newPassword);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("permitAll()")
    Account saveAndFlush(Account account);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @Query("""
        SELECT a FROM Account a 
        WHERE (:active IS NULL OR a.active = :active) 
        AND (:verified IS NULL OR a.verified = :verified)
    """)
    List<Account> findByActiveAndVerified(@Param("active") Boolean active, @Param("verified") Boolean verified);


    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("permitAll()")
    @Modifying
    @Query("UPDATE Account a SET a.lastSuccessfulLogin = :lastSuccessfulLogin, a.lastSuccessfulLoginIp = :lastSuccessfulLoginIp, a.loginAttempts = :loginAttempts  WHERE a.login = :login")
    void updateSuccessfulLogin(@Param("login") String login,
                               @Param("lastSuccessfulLogin") Date lastSuccessfulLogin,
                               @Param("lastSuccessfulLoginIp") String lastSuccessfulLoginIp,
                               @Param("loginAttempts") int loginAttempts);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("permitAll()")
    @Modifying
    @Query("UPDATE Account a SET a.lastFailedLogin = :lastFailedLogin, a.lastFailedLoginIp = :lastFailedLoginIp, a.loginAttempts = :loginAttempts WHERE a.login = :login")
    void updateFailedLogin(@Param("login") String login,
                           @Param("lastFailedLogin") Date lastFailedLogin,
                           @Param("lastFailedLoginIp") String lastFailedLoginIp,
                           @Param("loginAttempts") int loginAttempts);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("permitAll()")
    @Modifying
    @Query("""
        update Account a set a.lockedUntil = :lockedUntil, a.active = false where a.login = :login
    """)
    void lockTemporarily(@Param("login") String login, @Param("lockedUntil") Timestamp lockedUntil);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @Query("""
        select a from Account a where a.lockedUntil is not null
    """)
    List<Account> findByHasLockedUntil();

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.userRoles WHERE a.id = :id")
    Optional<Account> findByIdWithRoles(@Param("id") UUID id);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.userRoles WHERE a.login = :login")
    Optional<Account> findByLoginWithRoles(@Param("login") String login);
}
