package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class LoginDTO {
    @Size(min = AccountConsts.LOGIN_MIN, max = AccountConsts.LOGIN_MAX, groups = OnCreate.class)
    @NotBlank(groups = {OnCreate.class})
    private String login;

    @ToString.Exclude
    @Size(min = AccountConsts.PASSWORD_MIN, max = AccountConsts.PASSWORD_MAX, groups = OnCreate.class)
    @NotBlank(groups = {OnCreate.class})
    private String password;

    private LoginDTO() {}

    @Override
    public String toString() {
        return "LoginDTO{" +
                "login='" + login + '\'' +
                ", password='" + DTOConsts.PROTECTED + '\'' +
                '}';
    }
}
