package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;
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

    @Null(groups = {OnCreate.class, OnRead.class, OnUpdate.class})
    private Long version;

    @Null(groups = {OnCreate.class, OnRead.class, OnUpdate.class})
    private UUID clientId;

    @Setter
    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @DecimalMin(value = PeriodicSurveyConsts.WEIGHT_MIN, groups = {OnCreate.class, OnUpdate.class})
    @DecimalMax(value = PeriodicSurveyConsts.WEIGHT_MAX, groups = {OnCreate.class, OnUpdate.class})
    private Double weight;

    @Setter
    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = PeriodicSurveyConsts.BLOOD_PRESSURE_PATTERN, message = PeriodicSurveyConsts.BLOOD_PRESSURE_MESSAGE, groups = {OnCreate.class, OnUpdate.class})
    private String bloodPressure;

    @Setter
    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @DecimalMin(value = PeriodicSurveyConsts.BLOOD_SUGAR_MIN, groups = {OnCreate.class, OnUpdate.class})
    @DecimalMax(value = PeriodicSurveyConsts.BLOOD_SUGAR_MAX, groups = {OnCreate.class, OnUpdate.class})
    private Double bloodSugarLevel;

    @Setter
    @Null(groups = {OnCreate.class, OnUpdate.class})
    @NotNull(groups = OnRead.class)
    private Timestamp measurementDate;

    @Setter
    @NotNull(groups = {OnUpdate.class})
    @Null(groups = {OnCreate.class, OnRead.class})
    private String lockToken;

    @Override
    public String toString() {
        return "PeriodicSurveyDTO{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", weight=" + weight +
                ", bloodPressure='" + bloodPressure + '\'' +
                ", bloodSugarLevel=" + bloodSugarLevel +
                ", measurementDate=" + measurementDate +
                ", lockToken='" + DTOConsts.PROTECTED +
                '}';
    }
}
