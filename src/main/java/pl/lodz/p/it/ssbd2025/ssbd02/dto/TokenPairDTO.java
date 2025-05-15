package pl.lodz.p.it.ssbd2025.ssbd02.dto;

public record TokenPairDTO(
        String accessToken, String refreshToken, boolean is2fa
) { }
