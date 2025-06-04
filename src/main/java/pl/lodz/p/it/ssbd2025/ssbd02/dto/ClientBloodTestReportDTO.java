package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestResult;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class ClientBloodTestReportDTO {
    @NotNull(groups = {OnCreate.class, OnRead.class, OnUpdate.class})
    String lockToken;
    @NotNull(groups = {OnCreate.class, OnRead.class, OnUpdate.class})
    List<BloodTestResultDTO> results;

    @NotNull(groups = OnCreate.class)
    @Null(groups = {OnRead.class, OnUpdate.class})
    Client client;

    public ClientBloodTestReportDTO() {}

    @Override
    public String toString() {
        return "UpdateBloodTestReportDTO{" +
                "lockToken='" + DTOConsts.PROTECTED + '\'' +
                ", results=" + results +
                '}';
    }
}
