package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IFoodPyramidService;

import java.util.List;
import java.util.UUID;

public class FoodPyramidService implements IFoodPyramidService {
    @Override
    public FoodPyramid getById(UUID id) {
        return null;
    }

    @Override
    public List<FoodPyramid> getAll() {
        return List.of();
    }

    @Override
    public void updateAverageRating(UUID pyramidId) {

    }
}
