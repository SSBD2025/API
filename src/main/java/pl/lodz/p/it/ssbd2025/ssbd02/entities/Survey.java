package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
    @JoinColumn(name = SurveyConsts.COLUMN_CLIENT_ID, nullable = false, unique = true, updatable = false)
    private Client client;

    @Column(nullable = false)
    private double height;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @ToString.Exclude
    private Timestamp dateOfBirth;

    @Column(nullable = false, updatable = false)
    private boolean gender; //0 for female 1 for male

    @ElementCollection
    @Builder.Default
    @Column(name = SurveyConsts.COLUMN_DIET_PREFERENCES, nullable = false)
    private List<String> dietPreferences = new ArrayList<>();

    @ElementCollection
    @Builder.Default
    @Column(nullable = false)
    private List<String> allergies = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = SurveyConsts.COLUMN_ACTIVITY_LEVEL, nullable = false)
    private ActivityLevel activityLevel;

    @Column(nullable = false)
    private boolean smokes;

    @Column(name = SurveyConsts.COLUMN_DRINKS_ALCOHOL, nullable = false)
    private boolean drinksAlcohol;

    @ElementCollection
    @Builder.Default
    @Column(nullable = false)
    private List<String> illnesses = new ArrayList<>();

    @ElementCollection
    @Builder.Default
    @Column(nullable = false)
    private List<String> medications = new ArrayList<>();

    @Min(value = SurveyConsts.MEALS_PER_DAY_MIN)
    @Max(value = SurveyConsts.MEALS_PER_DAY_MAX)
    @Column(name = SurveyConsts.COLUMN_MEALS_PER_DAY, nullable = false)
    private int mealsPerDay;

    @Enumerated(EnumType.STRING)
    @Column(name = SurveyConsts.COLUMN_NUTRITION_GOAL, nullable = false)
    private NutritionGoal nutritionGoal;

    @ElementCollection
    @Builder.Default
    @Column(name = SurveyConsts.COLUMN_MEAL_TIMES, nullable = false)
    private List<Timestamp> mealTimes = new ArrayList<>();

//    @Lob
    @Column(name = SurveyConsts.COLUMN_EATING_HABITS, nullable = false, columnDefinition = "TEXT")
    private String eatingHabits;
}
