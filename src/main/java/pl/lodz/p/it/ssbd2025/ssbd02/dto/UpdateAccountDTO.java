package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;

public record UpdateAccountDTO(
        @NotNull
        @Size(max = AccountConsts.NAME_MAX)
        String firstName,

        @NotNull
        @Size(max = AccountConsts.NAME_MAX)
        String lastName,

        @NotNull
        String lockToken
) {
}
