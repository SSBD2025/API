package pl.lodz.p.it.ssbd2025.ssbd02.mod.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.MANDATORY)
public interface ClientModRepository extends AbstractRepository<Client> {

    @PreAuthorize("hasRole('CLIENT')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @Query("SELECT c FROM Client c WHERE c.account.login = :login")
    Optional<Client> findByLogin(@Param("login") String login);

    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    List<Client> findByDieticianId(@Param("dieticianId") UUID dieticianId);

    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @Query("""
        SELECT c FROM Client c WHERE c.dietician.id = :dieticianId
        AND (
            LOWER(c.account.firstName) LIKE LOWER(CONCAT('%', :searchPhrase, '%'))
            OR LOWER(c.account.lastName) LIKE LOWER(CONCAT('%', :searchPhrase, '%'))
            OR LOWER(c.account.email) LIKE LOWER(CONCAT('%', :searchPhrase, '%'))
        )
    """)
    List<Client> findByDieticianIdAndSearchPhrase(
            @Param("dieticianId") UUID dieticianId,
            @Param("searchPhrase") String searchPhrase);

    @PreAuthorize("hasRole('DIETICIAN')||hasRole('CLIENT')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    Optional<Client> findClientById(@Param("clientId") UUID id);

    @PreAuthorize("permitAll()")
    @Query("""
        SELECT c
        FROM Client c
        JOIN c.periodicSurveys ps
        GROUP BY c
        HAVING MAX(ps.measurementDate) < :thresholdDate
    """)
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    List<Client> findClientsWithLastPeriodicSurveyBefore(@Param("thresholdDate") Timestamp thresholdDate);

    @PreAuthorize("hasRole('CLIENT')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    Client saveAndFlush(Client client);
}
