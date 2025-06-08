package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.AssignDietPlanDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientFoodPyramidDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FoodPyramidDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientFoodPyramid;

import java.util.List;
import java.util.UUID;

public interface IClientFoodPyramidService {
    List<ClientFoodPyramid> getByClientId(UUID clientId);
    void assignFoodPyramidToClient(AssignDietPlanDTO dto);
    ClientFoodPyramid createAndAssignFoodPyramid(FoodPyramidDTO dto, SensitiveDTO clientId);
    void removeFoodPyramidFromClient(UUID clientId, UUID pyramidId);
    List<ClientFoodPyramidDTO> getClientPyramids();
    List<ClientFoodPyramidDTO> getClientPyramidsByDietician(UUID clientId);
}
