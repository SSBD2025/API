package pl.lodz.p.it.ssbd2025.ssbd02.mod.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestResult;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.util.Optional;
import java.util.UUID;

@Repository
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.MANDATORY)
public interface BloodTestResultRepository extends AbstractRepository<BloodTestResult> {

}
