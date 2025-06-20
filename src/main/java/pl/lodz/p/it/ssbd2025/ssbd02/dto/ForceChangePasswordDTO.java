package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class ForceChangePasswordDTO {

    @Size(min = AccountConsts.LOGIN_MIN, max = AccountConsts.LOGIN_MAX)
    @NotBlank
    @NotNull
    String login;

    @Size(min = AccountConsts.PASSWORD_MIN, max = AccountConsts.PASSWORD_MAX)
    @NotBlank
    @NotNull
    String oldPassword;

    @Size(min = AccountConsts.PASSWORD_MIN, max = AccountConsts.PASSWORD_MAX)
    @NotBlank
    @NotNull
    @Pattern(regexp = AccountConsts.PASSWORD_REGEX, message = AccountConsts.PASSWORD_MESSAGE)
    String newPassword;

    private ForceChangePasswordDTO() {}

    @Override
    public String toString() {
        return "ForceChangePasswordDTO{" +
                "login='" + login + '\'' +
                ", oldPassword='" + DTOConsts.PROTECTED + '\'' +
                ", newPassword='" + DTOConsts.PROTECTED + '\'' +
                '}';
    }
}
