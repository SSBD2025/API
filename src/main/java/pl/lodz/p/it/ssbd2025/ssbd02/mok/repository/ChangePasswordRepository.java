package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ChangePasswordEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;


@Repository
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Component("MOKChangePasswordRepository")
@Transactional(propagation = Propagation.MANDATORY)
public interface ChangePasswordRepository extends AbstractRepository<ChangePasswordEntity> {

    @Transactional(propagation = Propagation.MANDATORY)
    @PreAuthorize("permitAll()")
    ChangePasswordEntity saveAndFlush(ChangePasswordEntity entity);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    @PreAuthorize("permitAll()")
    ChangePasswordEntity findByAccount(Account account);
}
