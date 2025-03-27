package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.User;

import java.util.UUID;

@Repository
public interface MOKRepository extends JpaRepository<User, UUID> {
}
