package pl.lodz.p.it.ssbd2025.ssbd02.mod.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestResult;

import java.util.UUID;

public interface BloodTestResultRepository extends JpaRepository<BloodTestResult, UUID> {
}
