package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.BloodParameter;

import java.math.BigDecimal;

@Data
@Getter
@Setter(AccessLevel.NONE)
@AllArgsConstructor
//@NoArgsConstructor
public class BloodParameterDTO {

    @NotNull(groups = {OnCreate.class, OnRead.class})
    String name;

    @NotNull(groups = {OnCreate.class, OnRead.class})
    String description;

    @NotNull(groups = {OnCreate.class, OnRead.class})
    String unit;

    @NotNull(groups = {OnCreate.class, OnRead.class})
    Double standardMin;

    @NotNull(groups = {OnCreate.class, OnRead.class})
    Double standardMax;
}
