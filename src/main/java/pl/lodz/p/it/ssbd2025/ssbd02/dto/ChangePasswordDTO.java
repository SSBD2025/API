package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordDTO(
        @Size(min = 8, max = 60)
        @NotBlank
        String oldPassword,
        @Size(min = 8, max = 60)
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()]).+$",
                message = "Hasło musi zawierać małą literę, wielką literę, cyfrę i znak specjalny"
        )
        String newPassword
) {}
