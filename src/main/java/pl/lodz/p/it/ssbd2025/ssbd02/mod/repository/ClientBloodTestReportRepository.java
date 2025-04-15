package pl.lodz.p.it.ssbd2025.ssbd02.mod.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientBloodTestReport;

import java.util.UUID;

public interface ClientBloodTestReportRepository extends JpaRepository<ClientBloodTestReport, UUID> {
}
