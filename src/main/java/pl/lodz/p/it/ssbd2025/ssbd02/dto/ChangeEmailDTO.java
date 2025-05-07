package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ChangeEmailDTO(
        @Email
        @Size(max = 128)
        String email
) {}
