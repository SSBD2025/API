package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;

import java.util.UUID;

public interface IAlgorithmService {
    FoodPyramid generateFoodPyramid(UUID clientId);
}
