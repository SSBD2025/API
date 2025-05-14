package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import java.util.UUID;

public record AccountRoleDTO(
        UUID id,
        String roleName,
        Boolean active,
        Long version
) {
}
