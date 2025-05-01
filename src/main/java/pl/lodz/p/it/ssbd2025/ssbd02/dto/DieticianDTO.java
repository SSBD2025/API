package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.Valid;

public record DieticianDTO(
        @Valid UserRoleDTO.DieticianDTO dietician,
        @Valid AccountDTO account
) {
}
