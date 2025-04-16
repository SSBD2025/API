package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client",
    indexes = {
        @Index(name = "client_dietician_id_index", columnList = "dietician_id"),
    })
@DiscriminatorValue("CLIENT")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Client extends UserRole {

    @ManyToOne
    @JoinColumn(name = "dietician_id", nullable = true)
    private Dietician dietician;

    @OneToMany(mappedBy = "client")
    private List<PeriodicSurvey> periodicSurveys = new ArrayList<>();

    @OneToOne(mappedBy = "client")
    private Survey survey;

    @OneToMany(mappedBy = "client")
    private List<ClientBloodTestReport> bloodTestReports = new ArrayList<>();

    @OneToMany(mappedBy = "client")
    private List<ClientFoodPyramid> foodPyramidIds = new ArrayList<>();

    @OneToOne(mappedBy = "client", cascade = CascadeType.PERSIST)
    private DietaryRestrictions dietaryRestrictions;

    @OneToMany(mappedBy = "client")
    private List<Feedback> feedbacks = new ArrayList<>();
}
