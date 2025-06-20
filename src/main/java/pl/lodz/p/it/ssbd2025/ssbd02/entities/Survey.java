package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.ActivityLevel;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.NutritionGoal;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.SurveyConsts;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = SurveyConsts.TABLE_NAME)
@ToString(callSuper = true)
public class Survey extends AbstractEntity {
    @OneToOne(optional = false)
    @NotNull
    @JoinColumn(name = SurveyConsts.COLUMN_CLIENT_ID, nullable = false, unique = true, updatable = false)
    private Client client;

    @Min(value = SurveyConsts.HEIGHT_MIN)
    @Max(value = SurveyConsts.HEIGHT_MAX)
    @Column(nullable = false, updatable = true, unique = false)
    private double height;

    @Column(nullable = false, updatable = false, unique = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @ToString.Exclude
    private Timestamp dateOfBirth;

    @Column(nullable = false, updatable = false, unique = false)
    private boolean gender; //0 for female 1 for male

    @ElementCollection
    @Builder.Default
    @NotNull
    @Size(min = SurveyConsts.DIET_PREFERENCES_MIN, max = SurveyConsts.DIET_PREFERENCES_MAX)
    @Column(name = SurveyConsts.COLUMN_DIET_PREFERENCES, nullable = false, updatable = false, unique = false)
    private List<String> dietPreferences = new ArrayList<>();

    @ElementCollection
    @Builder.Default
    @NotNull
    @Size(min = SurveyConsts.ALLERGIES_MIN, max = SurveyConsts.ALLERGIES_MAX)
    @Column(nullable = false, updatable = false, unique = false)
    private List<String> allergies = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = SurveyConsts.COLUMN_ACTIVITY_LEVEL, nullable = false, updatable = true, unique = false)
    private ActivityLevel activityLevel;

    @Column(nullable = false, updatable = true, unique = false)
    private boolean smokes;

    @Column(name = SurveyConsts.COLUMN_DRINKS_ALCOHOL, nullable = false, updatable = true, unique = false)
    private boolean drinksAlcohol;

    @ElementCollection
    @Builder.Default
    @NotNull
    @Size(min = SurveyConsts.ILLNESSES_MIN, max = SurveyConsts.ILLNESSES_MAX)
    @Column(nullable = false, updatable = false, unique = false)
    private List<String> illnesses = new ArrayList<>();

    @ElementCollection
    @Builder.Default
    @NotNull
    @Size(min = SurveyConsts.MEDICATIONS_MIN, max = SurveyConsts.MEDICATIONS_MAX)
    @Column(nullable = false, updatable = false, unique = false)
    private List<String> medications = new ArrayList<>();

    @Min(value = SurveyConsts.MEALS_PER_DAY_MIN)
    @Max(value = SurveyConsts.MEALS_PER_DAY_MAX)
    @Column(name = SurveyConsts.COLUMN_MEALS_PER_DAY, nullable = false, updatable = true, unique = false)
    private int mealsPerDay;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = SurveyConsts.COLUMN_NUTRITION_GOAL, nullable = false, updatable = true, unique = false)
    private NutritionGoal nutritionGoal;

    @ElementCollection
    @Builder.Default
    @NotNull
    @Size(min = SurveyConsts.MEAL_TIMES_MIN, max = SurveyConsts.MEAL_TIMES_MAX)
    @Column(name = SurveyConsts.COLUMN_MEAL_TIMES, nullable = false, updatable = false, unique = false)
    private List<Timestamp> mealTimes = new ArrayList<>();

    @Size(min = SurveyConsts.EATING_HABITS_MIN, max = SurveyConsts.EATING_HABITS_MAX)
    @NotBlank
    @NotNull
    @Column(name = SurveyConsts.COLUMN_EATING_HABITS, nullable = false, columnDefinition = "TEXT", updatable = true, unique = false)
    private String eatingHabits;
}
