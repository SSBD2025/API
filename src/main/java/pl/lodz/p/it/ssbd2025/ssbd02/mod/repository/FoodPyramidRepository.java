package pl.lodz.p.it.ssbd2025.ssbd02.mod.repository;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Feedback;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.TokenEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.UserRole;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.util.Optional;
import java.util.UUID;

@Repository
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.MANDATORY)
public interface FoodPyramidRepository extends AbstractRepository<FoodPyramid> {
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    @PreAuthorize("hasRole('DIETICIAN') || hasRole('CLIENT')")
    FoodPyramid saveAndFlush(FoodPyramid foodPyramid);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('DIETICIAN')")
    FoodPyramid findByName(String name);

    @PreAuthorize("permitAll()") //FOR TESTS ONLY <- po co ???
    @Transactional(propagation = Propagation.MANDATORY) // <- brak readonly
    void delete(FoodPyramid foodPyramid);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("hasRole('DIETICIAN') || hasRole('CLIENT')")
    Optional<FoodPyramid> findById(UUID id);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("permitAll()")
    @Query("SELECT p FROM FoodPyramid p WHERE p.name = :name")
    FoodPyramid findByNameForTests(@Param("name") String name);
}
