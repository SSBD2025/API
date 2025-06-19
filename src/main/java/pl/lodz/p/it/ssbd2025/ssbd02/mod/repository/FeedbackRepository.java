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
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Feedback;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.MANDATORY)
public interface FeedbackRepository extends AbstractRepository<Feedback> {
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("hasRole('CLIENT')")
    Feedback saveAndFlush(Feedback feedback);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("hasRole('CLIENT')")
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.foodPyramid.id = :pyramidId")
    Double calculateAverageRating(@Param("pyramidId") UUID pyramidId);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("hasRole('CLIENT')")
    boolean existsByClientIdAndFoodPyramidId(UUID clientId, UUID pyramidId);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("hasRole('CLIENT')")
    void delete(Feedback feedback);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("hasRole('DIETICIAN')")
    List<Feedback> findAllByClient(Client client);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("hasRole('DIETICIAN')")
    List<Feedback> findAllByFoodPyramid(FoodPyramid pyramid);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('CLIENT')")
    Optional<Feedback> findByClientIdAndFoodPyramidId(UUID clientId, UUID pyramidId);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('CLIENT')")
    Optional<Feedback> findById(UUID id);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('DIETICIAN') || hasRole('CLIENT')")
    List<Feedback> findAll();

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('DIETICIAN') || hasRole('CLIENT')")
    List<Feedback> findByFoodPyramidId(UUID foodPyramidId);
}
