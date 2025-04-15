package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
}
