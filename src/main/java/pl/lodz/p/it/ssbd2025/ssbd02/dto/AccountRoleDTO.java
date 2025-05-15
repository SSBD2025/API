package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AccountRoleDTO(
        @NotNull
        String roleName,
        @NotNull
        Boolean active
) {
}
