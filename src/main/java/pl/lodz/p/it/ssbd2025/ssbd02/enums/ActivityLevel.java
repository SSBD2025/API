package pl.lodz.p.it.ssbd2025.ssbd02.enums;

import lombok.Getter;

@Getter
public enum ActivityLevel {
    SEDENTARY(1.2),
    LIGHT(1.375),
    MODERATE(1.55),
    ACTIVE(1.725),
    VERY_ACTIVE(1.9);

    private final double calorieModifier;

    ActivityLevel(double calorieModifier) {
        this.calorieModifier = calorieModifier;
    }

}
