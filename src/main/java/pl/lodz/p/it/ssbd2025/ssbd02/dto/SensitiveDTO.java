package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class SensitiveDTO {
    private String value;

    private SensitiveDTO(){}

    @Override
    public String toString() {
        return "SensitiveDTO{" +
                "value='" + DTOConsts.PROTECTED + '\'' +
                '}';
    }
}
