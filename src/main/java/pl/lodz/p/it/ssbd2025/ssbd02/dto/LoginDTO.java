package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {
    private String login;
    @ToString.Exclude
    private String password;
}
