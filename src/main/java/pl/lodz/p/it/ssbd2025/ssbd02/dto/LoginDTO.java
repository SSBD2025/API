package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {

    @Size(min = 4, max = 50, groups = OnCreate.class)
    @NotBlank(groups = {OnCreate.class})
    private String login;

    @ToString.Exclude
    @Size(min = 8, max = 60, groups = OnCreate.class)
    // regex not needed here
    @NotBlank(groups = {OnCreate.class})
    private String password;
}
