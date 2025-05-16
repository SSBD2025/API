package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;

public record ChangePasswordDTO(
        @Size(min = 8, max = 60)
        @NotBlank
        String oldPassword,
        @Size(min = 8, max = 60)
        @NotBlank
        @Pattern(regexp = AccountConsts.PASSWORD_REGEX, message = AccountConsts.PASSWORD_MESSAGE)
        String newPassword
) {}
