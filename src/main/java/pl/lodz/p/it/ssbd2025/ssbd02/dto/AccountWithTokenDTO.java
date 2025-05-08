package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import java.util.Collection;

public record AccountWithTokenDTO(
        AccountReadDTO account,
        String lockToken,
        Collection<AccountRolesProjection> roles
) {
}
