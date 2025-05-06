package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;

import java.util.List;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface DieticianRepository extends AbstractRepository<Dietician> {
    Dietician saveAndFlush(Dietician dietician);
}
