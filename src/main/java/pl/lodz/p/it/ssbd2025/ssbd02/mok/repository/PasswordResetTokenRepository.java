package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.JwtEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.PasswordResetToken;

import java.util.List;
import java.util.UUID;

@Repository
//@Component("MOKPasswordResetTokenRepository")
@Transactional(propagation = Propagation.MANDATORY)
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID>, AbstractRepository<PasswordResetToken> {
    PasswordResetToken findByAccount(Account account);

    PasswordResetToken findByToken(String passwordResetToken);

    void deleteByAccount(Account account);
}
