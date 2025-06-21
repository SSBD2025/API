package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.*;
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
    @Size(groups = {OnCreate.class}, min = BloodParameterConsts.NAME_SIZE_MIN, max = BloodParameterConsts.NAME_SIZE_MAX)
    String name;

    @NotNull(groups = {OnRead.class})
    @Null(groups = {OnCreate.class})
    @Size(groups = {OnCreate.class}, min = BloodParameterConsts.DESCRIPTION_SIZE_MIN, max = BloodParameterConsts.DESCRIPTION_SIZE_MAX)
    String description;

    @NotNull(groups = {OnRead.class})
    @NotBlank(groups = {OnRead.class})
    @Null(groups = {OnCreate.class})
    @Size(groups = {OnCreate.class}, min = BloodParameterConsts.UNIT_SIZE_MIN, max = BloodParameterConsts.UNIT_SIZE_MAX)
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
