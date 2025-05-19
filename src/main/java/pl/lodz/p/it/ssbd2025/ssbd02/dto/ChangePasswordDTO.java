package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;

public record ChangePasswordDTO(
        @Size(min = AccountConsts.PASSWORD_MIN, max = AccountConsts.PASSWORD_MAX)
        @NotBlank
        String oldPassword,

        @Size(min = AccountConsts.PASSWORD_MIN, max = AccountConsts.PASSWORD_MAX)
        @NotBlank
        @Pattern(regexp = AccountConsts.PASSWORD_REGEX, message = AccountConsts.PASSWORD_MESSAGE)
        String newPassword
) {}
