package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.BloodParameterConsts;


@Data
@Getter
@Setter(AccessLevel.NONE)
@AllArgsConstructor
public class BloodParameterDTO {

    @NotNull(groups = {OnCreate.class, OnRead.class})
    String name;

    @NotNull(groups = {OnRead.class})
    @Null(groups = {OnCreate.class})
    String description;

    @NotNull(groups = {OnRead.class})
    @NotBlank(groups = {OnRead.class})
    @Null(groups = {OnCreate.class})
    String unit;

    @NotNull(groups = {OnRead.class})
    @Null(groups = {OnCreate.class})
    @DecimalMin(value = BloodParameterConsts.STANDARD_MIN_MIN, groups = {OnRead.class})
    Double standardMin;

    @NotNull(groups = {OnRead.class})
    @Null(groups = {OnCreate.class})
    @DecimalMin(value = BloodParameterConsts.STANDARD_MAX_MIN, groups = {OnRead.class})
    Double standardMax;
}
