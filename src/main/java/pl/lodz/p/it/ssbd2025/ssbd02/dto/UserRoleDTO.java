package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

import java.util.UUID;

@Data
@Getter
@AllArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
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

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @Getter
    @ToString(callSuper = true)
    public static class AdminDTO extends UserRoleDTO { }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @Getter
    @ToString(callSuper = true)
    public static class DieticianDTO extends UserRoleDTO { }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @Getter
    @ToString(callSuper = true)
    public static class ClientDTO extends UserRoleDTO { }

    private UserRoleDTO() {}

    @Override
    public String toString() {
        return "UserRoleDTO{" +
                "roleName='" + roleName + '\'' +
                ", active=" + active +
                ", version=" + version +
                ", id=" + id +
                '}';
    }
}
