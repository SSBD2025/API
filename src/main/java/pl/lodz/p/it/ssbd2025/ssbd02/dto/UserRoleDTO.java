package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;

import java.util.UUID;

@Data
public class UserRoleDTO {
    @EqualsAndHashCode.Include
    @Null(groups = {OnCreate.class, OnUpdate.class})
    private UUID id;

    @Null(groups = OnCreate.class)
    @NotNull(groups = OnUpdate.class)
    private Long version;

    @Null(groups = {OnCreate.class, OnUpdate.class})
    private String roleName;

    @Null(groups = {OnCreate.class, OnUpdate.class})
    private Boolean active;

    @Getter
    @Setter
    @ToString(callSuper = true)
    public static class AdminDTO extends UserRoleDTO {//todo
    }

    @Getter @Setter
    @ToString(callSuper = true)
    public static class DieticianDTO extends UserRoleDTO {//todo
    }

    @Getter @Setter
    @ToString(callSuper = true)
    public static class ClientDTO extends UserRoleDTO {//todo
//        @NotNull(groups = OnCreate.class)
//        @Null(groups = OnUpdate.class)
//        private Survey survey;
    }
}
