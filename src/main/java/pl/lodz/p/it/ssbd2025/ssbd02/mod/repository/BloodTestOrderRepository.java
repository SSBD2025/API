package pl.lodz.p.it.ssbd2025.ssbd02.mod.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestOrder;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.MANDATORY)
public interface BloodTestOrderRepository extends AbstractRepository<BloodTestOrder> {
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("hasRole('DIETICIAN')")
    BloodTestOrder saveAndFlush(BloodTestOrder bloodTestOrder);

    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)//to ma byc readonly=false???
    @Query("SELECT COUNT(b) > 0 FROM BloodTestOrder b WHERE b.client = :client AND b.fulfilled = false")
    boolean hasUnfulfilledOrders(Client client);

    @PreAuthorize("hasRole('DIETICIAN')")//to ma byc readonly=false???
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    Optional<BloodTestOrder> findById(UUID orderId);

    @Query("""
    SELECT o FROM BloodTestOrder o
    JOIN FETCH o.client c
    JOIN FETCH c.account
    WHERE o.dietician.id = :dieticianId AND o.fulfilled = :fulfilled
""")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('DIETICIAN')")
    List<BloodTestOrder> getAllByDietician_IdAndFulfilled(
        @Param("dieticianId") UUID dieticianId,
        @Param("fulfilled") boolean fulfilled
    );

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('CLIENT')")
    Optional<BloodTestOrder> getFirstByClient_IdAndFulfilledOrderByOrderDateDesc(UUID clientId, boolean fulfilled);
}