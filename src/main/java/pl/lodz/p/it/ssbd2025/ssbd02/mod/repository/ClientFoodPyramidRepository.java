package pl.lodz.p.it.ssbd2025.ssbd02.mod.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientFoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientFoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.UserRole;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.MANDATORY)
public interface ClientFoodPyramidRepository extends AbstractRepository<ClientFoodPyramid> {
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("hasRole('DIETICIAN')")
    ClientFoodPyramid saveAndFlush(ClientFoodPyramid clientFoodPyramid);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('DIETICIAN') || hasRole('CLIENT')")
    boolean existsByClientAndFoodPyramid(Client client, FoodPyramid foodPyramid);

    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @Query("""
        select cfp from ClientFoodPyramid cfp where cfp.client.id=:clientId
""")
    List<ClientFoodPyramid> findAllByClientId(UUID clientId);

    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @Query("""
        SELECT cfp FROM ClientFoodPyramid cfp
        WHERE cfp.client.id = :clientId
        ORDER BY cfp.timestamp DESC
    """)
    List<ClientFoodPyramid> findByClientIdOrderByTimestampDesc(@Param("clientId") UUID clientId);
}
