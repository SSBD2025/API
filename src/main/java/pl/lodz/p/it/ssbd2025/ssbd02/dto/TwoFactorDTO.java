package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRequest;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class TwoFactorDTO {
        @NotNull(groups = OnRequest.class)
        String code;

        private TwoFactorDTO() {}

        @Override
        public String toString() {
                return "TwoFactorDTO{" +
                        "code='" + DTOConsts.PROTECTED + '\'' +
                        '}';
        }
}
