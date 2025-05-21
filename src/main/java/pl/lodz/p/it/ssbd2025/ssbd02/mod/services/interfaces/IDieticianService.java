package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;

import java.util.List;
import java.util.UUID;

public interface IDieticianService {
    Dietician getById(UUID id);
    List<Client> getClients(UUID dieticianId);
}
