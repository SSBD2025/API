package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import lombok.*;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class MinimalClientDTO {
    private String firstName;
    private String lastName;
    private String email;
}
