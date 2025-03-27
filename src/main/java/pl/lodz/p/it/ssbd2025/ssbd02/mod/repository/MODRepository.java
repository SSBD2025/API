package pl.lodz.p.it.ssbd2025.ssbd02.mod.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodMarker;

import java.util.UUID;

@Repository
public interface MODRepository extends JpaRepository<BloodMarker, UUID> {
}
