package pl.lodz.p.it.ssbd2025.ssbd02.enums;

import lombok.Getter;

@Getter
public enum NutritionGoal {
    REDUCTION(-300),
    MAINTENANCE(0),
    MASS_GAIN(300);

    private final int calorieModifier;

    NutritionGoal(int calorieModifier) {
        this.calorieModifier = calorieModifier;
    }
}
