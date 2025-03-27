package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "periodic_survey")
public class PeriodicSurvey {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;

    @Column(nullable = false)
    private double weight;

    @Column(name = "blood_pressure", nullable = false)
    private String bloodPressure;

    @Column(name = "measurement_date", nullable = false)
    private Timestamp measurementDate;
}
