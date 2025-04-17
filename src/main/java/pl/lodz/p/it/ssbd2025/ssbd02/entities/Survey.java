package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.ActivityLevel;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.NutritionGoal;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "survey")
public class Survey extends AbstractEntity {
    @OneToOne
    @JoinColumn(name = "client_id", nullable = false, unique = true, updatable = false)
    private Client client;

    @Column(nullable = false)
    private double height;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp dateOfBirth;

    @Column(nullable = false, updatable = false)
    private boolean gender; //0 for female 1 for male

    @ElementCollection
    @Column(name = "diet_preferences", nullable = false)
    private List<String> dietPreferences = new ArrayList<>();

    @ElementCollection
    @Column(nullable = false)
    private List<String> allergies = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false)
    private ActivityLevel activityLevel;

    @Column(nullable = false)
    private boolean smokes;

    @Column(name = "drinks_alcohol", nullable = false)
    private boolean drinksAlcohol;

    @ElementCollection
    @Column(nullable = false)
    private List<String> illnesses = new ArrayList<>();

    @ElementCollection
    @Column(nullable = false)
    private List<String> medications = new ArrayList<>();

    @Size(min = 1, max = 10)
    @Column(name = "meals_per_day", nullable = false)
    private int mealsPerDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "nutrition_goal", nullable = false)
    private NutritionGoal nutritionGoal;

    @ElementCollection
    @Column(name = "meal_times", nullable = false)
    private List<Timestamp> mealTimes = new ArrayList<>();

    @Lob
    @Column(name = "eating_habits", nullable = false)
    private String eatingHabits;
}
