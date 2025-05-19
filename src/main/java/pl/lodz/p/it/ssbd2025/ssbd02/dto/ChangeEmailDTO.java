package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;

public record ChangeEmailDTO(
        @Email
        @Size(max = AccountConsts.EMAIL_MAX)
        @NotBlank
        String email
) {}
