package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;

import java.sql.Timestamp;
import java.util.UUID;

public record AccountDTO(

        @Null(groups = {OnCreate.class, OnUpdate.class})
        @NotNull(groups = OnRead.class)
        UUID id,

        @Null(groups = {OnCreate.class, OnRead.class})
        @NotNull(groups = OnUpdate.class)
        Long version,

        @NotNull(groups = {OnCreate.class, OnRead.class})
        @Null(groups = OnUpdate.class)
        @Size(min = 4, max = 50, groups = OnCreate.class)
        String login,

        @Null(groups = {OnUpdate.class, OnRead.class})
        @Size(min = 8, max = 60, groups = OnCreate.class)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()]).{8,60}$",
                message = "Password must include at least: 1 lower case letter, 1 upper case letter, 1 number and 1 special character",
                groups = {OnCreate.class})
        String password,

        @NotNull(groups = OnRead.class)
        @Null(groups = {OnCreate.class, OnUpdate.class})
        Boolean verified,
        @Null(groups = {OnCreate.class, OnUpdate.class, OnRead.class})
        Boolean active,

        @NotBlank(groups = {OnCreate.class, OnRead.class})
        @Size(min = 1, max = 50, groups = {OnCreate.class, OnUpdate.class})
        String firstName,

        @NotBlank(groups = {OnCreate.class, OnRead.class})
        @Size(min = 1, max = 50, groups = {OnCreate.class, OnUpdate.class})
        String lastName,

        @NotNull(groups = {OnCreate.class, OnRead.class})
        @NotBlank(groups = OnCreate.class)
        @Email(groups = OnCreate.class)
        @Size(max = 128, groups = {OnCreate.class, OnUpdate.class})
        String email,

        @NotNull(groups = OnRead.class)
        @Null(groups = {OnCreate.class, OnUpdate.class})
        Timestamp lastSuccessfulLogin,

        @NotNull(groups = OnRead.class)
        @Null(groups = {OnCreate.class, OnUpdate.class})
        Timestamp lastFailedLogin,

        @NotNull(groups = OnRead.class)
        @NotNull(groups = {OnCreate.class, OnUpdate.class})
        Language language,

        @NotNull(groups = OnRead.class)
        @Null(groups = {OnCreate.class, OnUpdate.class})
        @Max(45)
        String lastSuccessfulLoginIp,

        @NotNull(groups = OnRead.class)
        @Null(groups = {OnCreate.class, OnUpdate.class})
        @Max(45)
        String lastFailedLoginIp,

        @NotNull(groups = {OnRead.class, OnCreate.class, OnUpdate.class})
        boolean twoFactorAuth,

        @NotNull(groups = {OnCreate.class})
        boolean reminded,

        @NotNull(groups = {OnCreate.class})
        int loginAttempts,

        @Null(groups = {OnCreate.class})
        Timestamp lockedUntil
) {
}
