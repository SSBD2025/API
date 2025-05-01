package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.UserRole;

public record AccountWithRolesDTO(
        AccountDTO accountDTO,
        Iterable<UserRoleDTO> userRoleDTOS
) {
}
