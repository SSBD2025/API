package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientService;

import java.util.List;
import java.util.UUID;

public class ClientService implements IClientService {
    @Override
    public Client getClientById(UUID id) {
        return null;
    }

    @Override
    public List<Client> getClientsByDieticianId(UUID dieticianId) {
        return List.of();
    }

    @Override
    public void assignDietician(UUID clientId, UUID dieticianId) {

    }
}
