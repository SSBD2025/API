package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.PeriodicSurveyConsts;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = PeriodicSurveyConsts.TABLE_NAME,
        indexes = {
                @Index(name = PeriodicSurveyConsts.CLIENT_ID_INDEX, columnList = PeriodicSurveyConsts.COLUMN_CLIENT_ID)
        })
@ToString(callSuper = true)
public class PeriodicSurvey extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = PeriodicSurveyConsts.COLUMN_CLIENT_ID, nullable = false, updatable = false)
    @ToString.Exclude
    private Client client;

    @Column(name = PeriodicSurveyConsts.COLUMN_WEIGHT, nullable = false, updatable = false)
    @DecimalMin(value = PeriodicSurveyConsts.WEIGHT_MIN)
    @DecimalMax(value = PeriodicSurveyConsts.WEIGHT_MAX)
    private double weight;

    @Column(name = PeriodicSurveyConsts.COLUMN_BLOOD_PRESSURE, nullable = false, updatable = false)
    @Pattern(regexp = PeriodicSurveyConsts.BLOOD_PRESSURE_PATTERN, message = PeriodicSurveyConsts.BLOOD_PRESSURE_MESSAGE)
    private String bloodPressure;

    @Column(name = PeriodicSurveyConsts.COLUMN_BLOOD_SUGAR_LEVEL, nullable = false, updatable = false)
    @DecimalMin(value = PeriodicSurveyConsts.BLOOD_SUGAR_MIN)
    @DecimalMax(value = PeriodicSurveyConsts.BLOOD_SUGAR_MAX)
    private double bloodSugarLevel;

    @Column(name = PeriodicSurveyConsts.COLUMN_MEASUREMENT_DATE, nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp measurementDate;
}
