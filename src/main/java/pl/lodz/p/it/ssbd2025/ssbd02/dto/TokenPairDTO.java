package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;

public record TokenPairDTO(
        @NotNull
        String accessToken,
        @NotNull
        String refreshToken,
        boolean is2fa
) { }
