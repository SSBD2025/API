package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "survey")
public class Survey extends AbstractEntity {
//    @OneToOne
//    @JoinColumn(name = "user_id", nullable = false, unique = true)
//    private User user;

    @Column(nullable = false)
    private double height;

    @Column(nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @ElementCollection
    @Column(name = "diet_preferences", nullable = false)
    private List<String> dietPreferences;

    @ElementCollection
    @Column(nullable = false)
    private List<String> allergies;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false)
    private ActivityLevel activityLevel;

    @Column(nullable = false)
    private boolean smokes;

    @Column(name = "drinks_alcohol", nullable = false)
    private boolean drinksAlcohol;

    @ElementCollection
    @Column(nullable = false)
    private List<String> illnesses;

    @ElementCollection
    @Column(nullable = false)
    private List<String> medications;

    @Column(name = "meals_per_day", nullable = false)
    private int mealsPerDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "nutrition_goal", nullable = false)
    private NutritionGoal nutritionGoal;

    @ElementCollection
    @Column(name = "meal_times", nullable = false)
    private List<Timestamp> mealTimes;

    @Lob
    @Column(name = "eating_habits", nullable = false)
    private String eatingHabits;
}
