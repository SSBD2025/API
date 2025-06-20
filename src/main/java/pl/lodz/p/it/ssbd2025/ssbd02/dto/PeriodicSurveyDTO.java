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

    private UUID id;

    private Long version;

    private UUID clientId;

    @Setter
    private Double weight;

    @Setter
    private String bloodPressure;

    @Setter
    private Double bloodSugarLevel;

    @Setter
    private Timestamp measurementDate;

    @Setter
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
