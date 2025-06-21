package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class SensitiveDTO {

    @NotBlank(groups = {OnCreate.class})
    private String value;

    private SensitiveDTO(){}

    @Override
    public String toString() {
        return "SensitiveDTO{" +
                "value='" + DTOConsts.PROTECTED + '\'' +
                '}';
    }
}
