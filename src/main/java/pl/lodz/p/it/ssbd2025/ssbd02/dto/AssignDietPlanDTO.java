package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignDietPlanDTO {

    @Null(groups = OnCreate.class)
    @NotNull(groups = OnUpdate.class)
    private UUID clientId;

    @NotNull(groups = OnCreate.class)
    @Null(groups = OnUpdate.class)
    private UUID foodPyramidId;

    @Override
    public String toString() {
        return "AssignDietPlanDTO{" +
                "clientId=" + clientId +
                ", foodPyramidId=" + foodPyramidId +
                '}';
    }
}
