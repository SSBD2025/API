package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAccountDTO(
        @NotNull
        @Size(max = 50)
        String firstName,
        @NotNull
        @Size(max = 50)
        String lastName,
        @NotNull
        String lockToken
) {
}
