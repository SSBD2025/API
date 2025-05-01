package pl.lodz.p.it.ssbd2025.ssbd02.mok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.common.AbstractRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;

import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ClientRepository extends AbstractRepository<Client> {

    Client saveAndFlush(Client client);
}
