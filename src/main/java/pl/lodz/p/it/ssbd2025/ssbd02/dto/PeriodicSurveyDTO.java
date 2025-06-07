package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.PeriodicSurveyConsts;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class PeriodicSurveyDTO {

    @Null(groups = {OnCreate.class, OnUpdate.class})
    @NotNull(groups = OnRead.class)
    private UUID id;

    @Null(groups = {OnCreate.class, OnRead.class})
    @NotNull(groups = OnUpdate.class)
    private Long version;

    @Null(groups = {OnCreate.class, OnRead.class})
    @NotNull(groups = OnUpdate.class)
    private UUID clientId;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @DecimalMin(value = PeriodicSurveyConsts.WEIGHT_MIN, groups = {OnCreate.class, OnUpdate.class})
    @DecimalMax(value = PeriodicSurveyConsts.WEIGHT_MAX, groups = {OnCreate.class, OnUpdate.class})
    private Double weight;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = PeriodicSurveyConsts.BLOOD_PRESSURE_PATTERN, message = PeriodicSurveyConsts.BLOOD_PRESSURE_MESSAGE, groups = {OnCreate.class, OnUpdate.class})
    private String bloodPressure;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @DecimalMin(value = PeriodicSurveyConsts.BLOOD_SUGAR_MIN, groups = {OnCreate.class, OnUpdate.class})
    @DecimalMax(value = PeriodicSurveyConsts.BLOOD_SUGAR_MAX, groups = {OnCreate.class, OnUpdate.class})
    private Double bloodSugarLevel;

    @Null(groups = {OnCreate.class, OnUpdate.class})
    @NotNull(groups = OnRead.class)
    private Timestamp measurementDate;

    @Override
    public String toString() {
        return "PeriodicSurveyDTO{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", weight=" + weight +
                ", bloodPressure='" + bloodPressure + '\'' +
                ", bloodSugarLevel=" + bloodSugarLevel +
                ", measurementDate=" + measurementDate +
                '}';
    }
}
