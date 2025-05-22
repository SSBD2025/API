package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IDieticianService;

import java.util.List;
import java.util.UUID;

public class DieticianService implements IDieticianService {
    @Override
    public Dietician getById(UUID id) {
        return null;
    }

    @Override
    public List<Client> getClients(UUID dieticianId) {
        return List.of();
    }
}
