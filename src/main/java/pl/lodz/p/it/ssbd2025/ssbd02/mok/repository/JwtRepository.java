package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.JwtEntity;

import java.util.List;
import java.util.UUID;

@Repository
@Component("MOKJwtRepository")
@Transactional(propagation = Propagation.MANDATORY)
public interface JwtRepository extends JpaRepository<JwtEntity, UUID>, AbstractRepository<JwtEntity> {
    List<JwtEntity> findByAccount(Account account);

    void deleteAllByAccount(Account account);

    boolean existsByToken(String token);
}
