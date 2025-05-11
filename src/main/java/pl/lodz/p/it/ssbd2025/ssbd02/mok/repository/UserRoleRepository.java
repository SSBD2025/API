package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.*;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.util.List;
import java.util.UUID;

@Repository
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.MANDATORY)
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    List<Admin> findAdminsByAccount(Account account);
    List<Client> findClientsByAccount(Account account);
    List<Dietician> findDieticiansByAccount(Account account);
}
