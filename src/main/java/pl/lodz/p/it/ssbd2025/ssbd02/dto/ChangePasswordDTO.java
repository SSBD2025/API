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
                message = "Password must contain a lowercase letter, an uppercase letter, a digit, and a special character."
        )
        String newPassword
) {}
