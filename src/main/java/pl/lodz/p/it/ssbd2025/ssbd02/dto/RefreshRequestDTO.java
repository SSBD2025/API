package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;

public record RefreshRequestDTO(
        @NotBlank(groups = OnCreate.class)
        @Pattern(
                regexp = "^[A-Za-z0-9_-]{2,}(?:\\.[A-Za-z0-9_-]{2,}){2}$",
                message = "Invalid JWT format",
                groups = {OnCreate.class}
        )
        String refreshToken
) { }
