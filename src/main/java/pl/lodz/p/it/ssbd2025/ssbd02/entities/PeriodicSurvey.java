package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "periodic_survey",
    indexes = {
        @Index(name = "ps_client_id_index", columnList = "client_id")
    })
public class PeriodicSurvey extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false, updatable = false)
    private Client client;

    @Column(nullable = false, updatable = false)
    @Size(min = 20, max = 350)
    private double weight;

    @Column(name = "blood_pressure", nullable = false, updatable = false)
    @Pattern(regexp = "(\\d{2,3})/(\\d{2,3})", message = "Blood pressure must be in the format 'XX/XX'")
    private String bloodPressure;

    @Column(name = "blood_sugar_level", nullable = false, updatable = false)
    @Size(min = 10, max = 500)
    private double bloodSugarLevel;

    @Column(name = "measurement_date", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp measurementDate;
}
