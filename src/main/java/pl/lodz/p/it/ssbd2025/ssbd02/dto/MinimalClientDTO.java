package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;

import java.util.UUID;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class MinimalClientDTO {
    @Null(groups = {OnCreate.class, OnUpdate.class})
    @NotNull(groups = OnRead.class)
    private UUID id;

    @NotBlank(groups = {OnCreate.class, OnRead.class})
    @Size(min = AccountConsts.NAME_MIN, max = AccountConsts.NAME_MAX, groups = {OnCreate.class, OnUpdate.class})
    private String firstName;

    @NotBlank(groups = {OnCreate.class, OnRead.class})
    @Size(min = AccountConsts.NAME_MIN, max = AccountConsts.NAME_MAX, groups = {OnCreate.class, OnUpdate.class})
    private String lastName;

    @NotNull(groups = {OnCreate.class, OnRead.class})
    @NotBlank(groups = OnCreate.class)
    @Email(groups = OnCreate.class)
    @Size(max = AccountConsts.EMAIL_MAX, groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = AccountConsts.EMAIL_REGEX, message = AccountConsts.EMAIL_MESSAGE, groups = {OnCreate.class, OnUpdate.class})
    private String email;
}