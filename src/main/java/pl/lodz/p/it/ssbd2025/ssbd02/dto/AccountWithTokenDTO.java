package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import org.springframework.validation.annotation.Validated;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRequest;

import java.util.Collection;

public record AccountWithTokenDTO(
        @Validated(OnRequest.class) AccountDTO account,
        String lockToken,
        Iterable<AccountRoleDTO> roles
) {
}
