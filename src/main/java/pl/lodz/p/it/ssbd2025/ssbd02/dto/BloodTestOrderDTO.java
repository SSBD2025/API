package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.BloodParameter;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.BloodTestOrderConsts;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@Setter
@Getter
@AllArgsConstructor
public class BloodTestOrderDTO {
    @NotNull(groups = {OnRead.class, OnCreate.class})
    private UUID clientId;
    @NotNull(groups = OnRead.class)
    private UUID dieticianId;
    @NotNull(groups = OnRead.class)
    private Timestamp orderDate;
    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Size(min = BloodTestOrderConsts.DESCRIPTION_MIN, max = BloodTestOrderConsts.DESCRIPTION_MAX, groups = {OnCreate.class, OnUpdate.class})
    private String description;
    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Size(min = BloodTestOrderConsts.PARAMETERS_MIN, max = BloodTestOrderConsts.PARAMETERS_MAX, groups = {OnCreate.class, OnUpdate.class})
    private List<BloodParameter> parameters;
    private boolean fulfilled;

    public BloodTestOrderDTO() {}

    @Override
    public String toString() {
        return "BloodTestOrderDTO{" +
                "clientId=" + clientId +
                ", dieticianId=" + dieticianId +
                ", orderDate=" + orderDate +
                ", description='" + description + '\'' +
                ", parameters=" + parameters +
                ", fulfilled=" + fulfilled +
                '}';
    }
}
