package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.Valid;

public record ClientDTO(
        @Valid UserRoleDTO.ClientDTO client,
        @Valid AccountDTO account
) {
}
