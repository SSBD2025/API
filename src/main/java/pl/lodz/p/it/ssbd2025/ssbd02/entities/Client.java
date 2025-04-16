package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client",
    indexes = {
        @Index(name = "client_dietician_id_index", columnList = "dietician_id"),
        @Index(name = "client_food_pyramid_id_index", columnList = "food_pyramid_id")
    })
@DiscriminatorValue("CLIENT")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Client extends UserRole {

    @ManyToOne
    @JoinColumn(name = "dietician_id")
    private Dietician dietician;

    @OneToMany(mappedBy = "client")
    private List<PeriodicSurvey> periodicSurveys = new ArrayList<>();

    @OneToOne(mappedBy = "client")
    private Survey survey;

    @OneToMany(mappedBy = "client")
    private List<ClientBloodTestReport> bloodTestReports = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "food_pyramid_id")
    private FoodPyramid foodPyramid;

    @OneToOne(mappedBy = "client", cascade = CascadeType.PERSIST)
    private DietaryRestrictions dietaryRestrictions;

    @OneToMany(mappedBy = "client")
    private List<Feedback> feedbacks = new ArrayList<>();
}
