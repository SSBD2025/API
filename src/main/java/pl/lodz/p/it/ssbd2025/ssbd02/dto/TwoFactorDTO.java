package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRequest;

public record TwoFactorDTO(
        @NotNull(groups = OnRequest.class)
        String code
) {
}
