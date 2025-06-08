package pl.lodz.p.it.ssbd2025.ssbd02.mod.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientBloodTestReport;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.TokenEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;

import java.util.List;
import java.util.UUID;

@Transactional(propagation = Propagation.MANDATORY)
public interface ClientBloodTestReportRepository extends JpaRepository<ClientBloodTestReport, UUID> {

    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @Query("""
        select r from ClientBloodTestReport r where r.client.id=:clientId
    """)
    List<ClientBloodTestReport> findAllByClientId(UUID clientId);

    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @Override
    <S extends ClientBloodTestReport> S saveAndFlush(S entity);
}
