package pl.lodz.p.it.ssbd2025.ssbd02.mod.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.*;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.MANDATORY)
public interface ClientBloodTestReportRepository extends JpaRepository<ClientBloodTestReport, UUID> {

//    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    @PreAuthorize("permitAll()")//<- co to ma byc???
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @Query("""
        select r from ClientBloodTestReport r where r.client.id=:clientId
    """)
    List<ClientBloodTestReport> findAllByClientId(UUID clientId);

    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @Override
    <S extends ClientBloodTestReport> S saveAndFlush(S entity);

    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    Optional<ClientBloodTestReport> findFirstByClient_IdOrderByTimestampDesc (UUID clientId);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    Optional<ClientBloodTestReport> findById(UUID id);
}
