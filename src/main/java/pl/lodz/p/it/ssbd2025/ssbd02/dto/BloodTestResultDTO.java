package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientBloodTestReport;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

import java.util.UUID;

@Data
@Getter
@AllArgsConstructor
public class BloodTestResultDTO {
    @Setter
    @Null(groups = {OnCreate.class, OnUpdate.class})
    @NotNull(groups = OnRead.class)
    UUID id;

    @Setter(AccessLevel.NONE)
    @Null(groups = {OnCreate.class, OnRead.class, OnUpdate.class})
    Long version;

    @Setter
    @NotNull(groups = {OnRead.class, OnUpdate.class})
    @Null(groups = {OnCreate.class})
    String lockToken;

    @Setter
    @NotNull(groups = {OnCreate.class, OnRead.class, OnUpdate.class})
    Double result;

    @Valid
    @Setter
    @NotNull(groups = {OnCreate.class, OnRead.class, OnUpdate.class})
    BloodParameterDTO bloodParameter;

    @JsonIgnore
    @Setter(AccessLevel.NONE)
    @Null(groups = {OnCreate.class, OnUpdate.class, OnRead.class})
    ClientBloodTestReport report;

    public BloodTestResultDTO() {}

    @Override
    public String toString() {
        return "BloodTestResultDTO{" +
                "lockToken='" + DTOConsts.PROTECTED + '\'' +
                ", result='" + result + '\'' +
                ", bloodParameter=" + bloodParameter +
                '}';
    }
}
