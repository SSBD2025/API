package pl.lodz.p.it.ssbd2025.ssbd02.mod.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.MANDATORY)
public interface DieticianModRepository extends AbstractRepository<Dietician> {

    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @Query("SELECT d FROM Dietician d WHERE d.account.login = :login")
    Optional<Dietician> findByLogin(@Param("login") String login);

    @PreAuthorize("hasRole('CLIENT')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @Query("SELECT d FROM Dietician d WHERE SIZE(d.clients) < 10")
    List<Dietician> getAllAvailableDietians();

    @PreAuthorize("hasRole('CLIENT')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @Query("""
        SELECT d FROM Dietician d WHERE SIZE(d.clients) < 10
        AND (
            LOWER(d.account.firstName) LIKE LOWER(CONCAT('%', :searchPhrase, '%'))
            OR LOWER(d.account.lastName) LIKE LOWER(CONCAT('%', :searchPhrase, '%'))
            OR LOWER(d.account.email) LIKE LOWER(CONCAT('%', :searchPhrase, '%'))
        )
    """)
    List<Dietician> getAllAvailableDieticiansBySearchPhrase(@Param("searchPhrase") String searchPhrase);

    @PreAuthorize("hasRole('CLIENT')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    Optional<Dietician> findDieticianById(UUID dieticianId);

    @PreAuthorize("hasRole('CLIENT')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    Dietician saveAndFlush(Dietician dietician);
}
