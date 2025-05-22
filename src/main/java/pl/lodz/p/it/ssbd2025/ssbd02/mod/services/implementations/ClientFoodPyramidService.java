package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientFoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientFoodPyramidService;

import java.util.List;
import java.util.UUID;

public class ClientFoodPyramidService implements IClientFoodPyramidService {
    @Override
    public List<ClientFoodPyramid> getByClientId(UUID clientId) {
        return List.of();
    }

    @Override
    public ClientFoodPyramid assignFoodPyramidToClient(UUID clientId, UUID pyramidId) {
        return null;
    }

    @Override
    public void removeFoodPyramidFromClient(UUID clientId, UUID pyramidId) {

    }
}
