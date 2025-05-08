package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountRolesProjection;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;

import java.security.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
@Component("MOKAccountRepository")
@Transactional(propagation = Propagation.MANDATORY)
public interface AccountRepository extends AbstractRepository<Account> {
//    Account findByLogin(@NotBlank @Size(min = 4, max = 50) String login);

    Account findByLogin(@NotBlank @Size(min = 4, max = 50) String login);

    Account findByEmail(@Email String email);

    @Query("""
        select a.login as login, ur.roleName as roleName, ur.active as active
        from Account a join a.userRoles ur
        where a.login = :login
    """)
    List<AccountRolesProjection> findAccountRolesByLogin(@NotBlank @Size(min = 4, max = 50) String login);

    @Modifying
    @Query("UPDATE Account a SET a.password = :newPassword WHERE a.login = :login")
    void updatePassword(@Param("login") String login, @Param("newPassword") String newPassword);

    Account saveAndFlush(Account account);
//    void updatePassword(@NonNull @NotBlank @NotEmpty String login, @NonNull @NotBlank @NotEmpty String newPassword);

    @Query("""
        SELECT a FROM Account a 
        WHERE (:active IS NULL OR a.active = :active) 
        AND (:verified IS NULL OR a.verified = :verified)
    """)
    List<Account> findByActiveAndVerified(@Param("active") Boolean active, @Param("verified") Boolean verified);


    @Modifying
    @Query("UPDATE Account a SET a.lastSuccessfulLogin = :lastSuccessfulLogin, a.lastSuccessfulLoginIp = :lastSuccessfulLoginIp WHERE a.login = :login")
    void updateSuccessfulLogin(@Param("login") String login,
                               @Param("lastSuccessfulLogin") Date lastSuccessfulLogin,
                               @Param("lastSuccessfulLoginIp") String lastSuccessfulLoginIp);

    @Modifying
    @Query("UPDATE Account a SET a.lastFailedLogin = :lastFailedLogin, a.lastFailedLoginIp = :lastFailedLoginIp WHERE a.login = :login")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateFailedLogin(@Param("login") String login,
                           @Param("lastFailedLogin") Date lastFailedLogin,
                           @Param("lastFailedLoginIp") String lastFailedLoginIp);
}
