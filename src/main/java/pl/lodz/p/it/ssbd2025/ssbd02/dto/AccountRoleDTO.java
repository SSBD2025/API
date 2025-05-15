package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import java.util.UUID;

public record AccountRoleDTO(
        String roleName,
        Boolean active
) {
}
