package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.UserRole;

import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
}
