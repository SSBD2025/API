package pl.lodz.p.it.ssbd2025.ssbd02.mod.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.PeriodicSurvey;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;


@Repository
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.MANDATORY)
public interface PeriodicSurveyRepository extends AbstractRepository<PeriodicSurvey> {

    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("hasRole('CLIENT')")
    PeriodicSurvey saveAndFlush(PeriodicSurvey periodicSurvey);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('CLIENT')")
    boolean existsByClientAndMeasurementDateAfter(Client client, Timestamp measurementDate);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('CLIENT') || hasRole('DIETICIAN')")
    Page<PeriodicSurvey> findByClientId(UUID clientId, Pageable pageable);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("permitAll()")
    Optional<PeriodicSurvey> findById(UUID id);
}
