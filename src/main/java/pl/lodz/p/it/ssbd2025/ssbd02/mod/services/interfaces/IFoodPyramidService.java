package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.FoodPyramidDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;

import java.util.List;
import java.util.UUID;

public interface IFoodPyramidService {
    FoodPyramid getById(UUID id);
    List<FoodPyramidDTO> getAllFoodPyramids();
    void updateAverageRating(UUID pyramidId);
}
