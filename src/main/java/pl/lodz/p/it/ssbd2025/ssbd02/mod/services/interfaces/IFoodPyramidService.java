package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;

import java.util.List;
import java.util.UUID;

public interface IFoodPyramidService {
    FoodPyramid getById(UUID id);
    List<FoodPyramid> getAll();
    void updateAverageRating(UUID pyramidId);
}
