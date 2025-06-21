package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.validation.ValidLogin;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    @NotBlank(groups = OnCreate.class)
    @Size(min = AccountConsts.LOGIN_MIN, max = AccountConsts.LOGIN_MAX, groups = OnCreate.class)
    @ValidLogin(groups = OnCreate.class)
    @Null(groups = OnUpdate.class)
    private String login;

    @ToString.Exclude
    @NotBlank(groups = OnCreate.class)
    @Size(min = AccountConsts.PASSWORD_MIN, max = AccountConsts.PASSWORD_MAX, groups = OnCreate.class)
    @Pattern(regexp = AccountConsts.PASSWORD_REGEX, message = AccountConsts.PASSWORD_MESSAGE, groups = OnCreate.class)
    private String password;

    @Override
    public String toString() {
        return "LoginDTO{" +
                "login='" + login + '\'' +
                ", password='" + DTOConsts.PROTECTED + '\'' +
                '}';
    }
}
