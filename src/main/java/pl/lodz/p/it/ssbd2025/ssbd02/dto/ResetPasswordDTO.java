package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRequest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnReset;

public record ResetPasswordDTO(
        @Email(groups = {OnRequest.class, OnReset.class})
        @NotNull(groups = {OnRequest.class, OnReset.class})
        String email,
        @Null(groups = OnRequest.class)
        @NotNull(groups = OnReset.class)
        String password

) {
}
