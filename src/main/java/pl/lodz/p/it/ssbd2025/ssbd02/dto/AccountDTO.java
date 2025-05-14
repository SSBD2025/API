package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;

import java.sql.Timestamp;
import java.util.UUID;

public record AccountDTO(

        @Null(groups = {OnCreate.class, OnUpdate.class})
        UUID id,

        @Null(groups = OnCreate.class)
        @NotNull(groups = OnUpdate.class)
        Long version,

        @Null(groups = OnUpdate.class)
        @Size(min = 4, max = 50, groups = OnCreate.class)
        String login,

        @Null(groups = OnUpdate.class)
        @Size(min = 8, max = 60, groups = OnCreate.class)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()]).{8,60}$",
                message = "Password must include at least: 1 lower case letter, 1 upper case letter, 1 number and 1 special character",
                groups = {OnCreate.class})
        String password,

        @Null(groups = {OnCreate.class, OnUpdate.class})
        Boolean verified,
        @Null(groups = {OnCreate.class, OnUpdate.class})
        Boolean active,

        @NotBlank(groups = OnCreate.class)
        @Size(min = 1, max = 50, groups = {OnCreate.class, OnUpdate.class})
        String firstName,

        @NotBlank(groups = OnCreate.class)
        @Size(min = 1, max = 50, groups = {OnCreate.class, OnUpdate.class})
        String lastName,

        @NotBlank(groups = OnCreate.class)
        @Email(groups = OnCreate.class)
        @Size(max = 128, groups = {OnCreate.class, OnUpdate.class})
        String email,

        @Null(groups = {OnCreate.class, OnUpdate.class})
        Timestamp lastSuccessfulLogin,

        @Null(groups = {OnCreate.class, OnUpdate.class})
        Timestamp lastFailedLogin,

        @NotNull(groups = {OnCreate.class, OnUpdate.class})
        Language language,

        @Null(groups = {OnCreate.class, OnUpdate.class})
        @Max(45)
        String lastSuccessfulLoginIp,

        @Null(groups = {OnCreate.class, OnUpdate.class})
        @Max(45)
        String lastFailedLoginIp
) {
}
