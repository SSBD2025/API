package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountRolesProjection;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.*;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.MANDATORY)
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    //    List<Admin> findAdminsByAccount(Account account);
//    List<Client> findClientsByAccount(Account account);
//    List<Dietician> findDieticiansByAccount(Account account);
//    @Override
//    <S extends UserRole> S saveAndFlush(S entity);
    UserRole saveAndFlush(UserRole userRole);

    @Modifying
    @Query("UPDATE UserRole ur SET ur.active = :active WHERE ur.account.login = :login AND ur.roleName = :roleName")
    void updateRoleActiveStatus(@Param("login") String login,
                                @Param("roleName") String roleName,
                                @Param("active") boolean active);

    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur WHERE ur.account.login = :login AND ur.roleName = :roleName")
    boolean existsByLoginAndRoleName(@Param("login") String login, @Param("roleName") String roleName);

    @Query("SELECT ur.active FROM UserRole ur WHERE ur.account.login = :login AND ur.roleName = :roleName")
    Optional<Boolean> findActiveByLoginAndRoleName(@Param("login") String login, @Param("roleName") String roleName);
}
