package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;

import java.util.UUID;

@Repository
@Component("MOKAccountRepository")
@Transactional(propagation = Propagation.MANDATORY)
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Account findByLogin(@NotBlank @Size(min = 4, max = 50) String login);

//    void updatePassword(@NonNull @NotBlank @NotEmpty String login, @NonNull @NotBlank @NotEmpty String newPassword);
}
