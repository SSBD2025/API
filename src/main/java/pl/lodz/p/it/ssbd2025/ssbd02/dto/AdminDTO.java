package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.Valid;

public record AdminDTO(
        @Valid UserRoleDTO.AdminDTO admin,
        @Valid AccountDTO account
) {
}
