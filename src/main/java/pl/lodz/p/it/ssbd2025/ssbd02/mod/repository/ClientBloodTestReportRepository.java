package pl.lodz.p.it.ssbd2025.ssbd02.mod.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.*;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.MANDATORY)
public interface ClientBloodTestReportRepository extends AbstractRepository<ClientBloodTestReport> {
    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @Query("""
        select r from ClientBloodTestReport r where r.client.id=:clientId
    """)
    List<ClientBloodTestReport> findAllByClientId(UUID clientId);

    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    ClientBloodTestReport saveAndFlush(ClientBloodTestReport report);

    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    Optional<ClientBloodTestReport> findFirstByClient_IdOrderByTimestampDesc (UUID clientId);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    Optional<ClientBloodTestReport> findById(UUID id);
}
