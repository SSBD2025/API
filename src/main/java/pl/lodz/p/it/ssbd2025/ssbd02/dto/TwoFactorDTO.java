package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;

public record TwoFactorDTO(
        @NotNull
        String login,
        @NotNull
        String code
) {
}
