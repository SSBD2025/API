package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
}
