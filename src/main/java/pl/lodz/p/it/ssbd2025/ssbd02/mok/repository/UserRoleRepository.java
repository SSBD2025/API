package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountRolesProjection;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.*;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
public interface UserRoleRepository extends AbstractRepository<UserRole> {
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("hasRole('ADMIN')")
    UserRole saveAndFlush(UserRole userRole);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("hasRole('ADMIN')")
    @Modifying
    @Query("UPDATE UserRole ur SET ur.active = :active WHERE ur.account.login = :login AND ur.roleName = :roleName")
    void updateRoleActiveStatus(@Param("login") String login,
                                @Param("roleName") String roleName,
                                @Param("active") boolean active);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur WHERE ur.account.login = :login AND ur.roleName = :roleName")
    boolean existsByLoginAndRoleName(@Param("login") String login, @Param("roleName") String roleName);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    @Query("SELECT ur.active FROM UserRole ur WHERE ur.account.login = :login AND ur.roleName = :roleName")
    Optional<Boolean> findActiveByLoginAndRoleName(@Param("login") String login, @Param("roleName") String roleName);
}
