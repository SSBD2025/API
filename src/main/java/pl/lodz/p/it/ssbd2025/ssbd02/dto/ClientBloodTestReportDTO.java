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

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class ClientBloodTestReportDTO {

    @Setter
    @Null(groups = {OnCreate.class, OnUpdate.class})
    @NotNull(groups = OnRead.class)
    UUID id;

    @Null(groups = {OnCreate.class, OnRead.class, OnUpdate.class})
    Long version;

    @Null(groups = {OnRead.class, OnUpdate.class, OnCreate.class})
    Client client;

    @Setter
    @NotNull(groups = {OnRead.class})
    @Null(groups = {OnUpdate.class, OnCreate.class})
    private Timestamp timestamp;

    @Setter
    @NotNull(groups = {OnCreate.class, OnRead.class, OnUpdate.class})
    List<BloodTestResultDTO> results;

    @Setter
    @NotNull(groups = {OnRead.class, OnUpdate.class})
    @Null(groups = {OnCreate.class})
    String lockToken;

    public ClientBloodTestReportDTO() {}

    @Override
    public String toString() {
        return "ClientBloodTestReportDTO{" +
                "client=" + client +
                ", timestamp=" + timestamp +
                ", results=" + DTOConsts.PROTECTED +
                ", lockToken='" + DTOConsts.PROTECTED + '\'' +
                '}';
    }
}
