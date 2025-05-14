package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import java.util.List;

public record AccountWithRolesDTO(
        AccountDTO accountDTO,
        List<AccountRoleDTO> userRoleDTOS
) {
}
