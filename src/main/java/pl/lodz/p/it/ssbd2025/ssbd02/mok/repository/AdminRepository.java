package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Admin;

import java.util.UUID;

public interface AdminRepository extends JpaRepository<Admin, UUID> {
}
