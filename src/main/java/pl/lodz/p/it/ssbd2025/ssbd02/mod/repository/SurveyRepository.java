package pl.lodz.p.it.ssbd2025.ssbd02.mod.repository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.util.Optional;
import java.util.UUID;

@Repository
@Component("MODSurveyRepository")
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
public interface SurveyRepository extends AbstractRepository<Survey> {

    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("hasRole('CLIENT')")
    Survey saveAndFlush(Survey survey);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('CLIENT')")
    boolean existsByClientId(UUID clientId);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasAnyRole('CLIENT', 'DIETICIAN')")
    Optional<Survey> findByClientId(UUID id);
}
