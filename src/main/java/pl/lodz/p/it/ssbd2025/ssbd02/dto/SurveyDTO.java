package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.ActivityLevel;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.NutritionGoal;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@Setter
@Getter
@AllArgsConstructor
public class SurveyDTO {

    @NotNull(groups = OnRead.class)
    private UUID clientId;

    @NotNull(groups = {OnCreate.class})
    @Positive
    private double height;

    @NotNull(groups = OnCreate.class)
    private Timestamp dateOfBirth;

    @NotNull(groups = {OnCreate.class})
    private Boolean gender;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Size(min = 0)
    private List<String> dietPreferences;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Size(min = 0)
    private List<String> allergies;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    private ActivityLevel activityLevel;

    private boolean smokes;

    private boolean drinksAlcohol;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Size(min = 0)
    private List<String> illnesses;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Size(min = 0)
    private List<String> medications;

    @Min(value = 1, groups = {OnCreate.class, OnUpdate.class})
    @Max(value = 10, groups = {OnCreate.class, OnUpdate.class})
    private int mealsPerDay;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    private NutritionGoal nutritionGoal;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Size(min = 1, groups = {OnCreate.class, OnUpdate.class})
    private List<Timestamp> mealTimes;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    private String eatingHabits;

    private SurveyDTO() {}

    @Override
    public String toString() {
        return "SurveyDTO{" +
                "clientId=" + clientId +
                ", height=" + height +
                ", dateOfBirth=" + dateOfBirth +
                ", gender=" + gender +
                ", dietPreferences=" + dietPreferences +
                ", allergies=" + allergies +
                ", activityLevel=" + activityLevel +
                ", smokes=" + smokes +
                ", drinksAlcohol=" + drinksAlcohol +
                ", illnesses=" + illnesses +
                ", medications=" + medications +
                ", mealsPerDay=" + mealsPerDay +
                ", nutritionGoal=" + nutritionGoal +
                ", mealTimes=" + mealTimes +
                ", eatingHabits='" + eatingHabits + '\'' +
                '}';
    }
}
