package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientFoodPyramid;

import java.util.List;
import java.util.UUID;

public interface IClientFoodPyramidService {
    List<ClientFoodPyramid> getByClientId(UUID clientId);
    ClientFoodPyramid assignFoodPyramidToClient(UUID clientId, UUID pyramidId);
    void removeFoodPyramidFromClient(UUID clientId, UUID pyramidId);
}
