package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class TokenPairDTO {
        @NotNull
        String accessToken;
        @NotNull
        String refreshToken;
        boolean is2fa;

        @Override
        public String toString() {
                return "TokenPairDTO{" +
                        "accessToken='" + DTOConsts.PROTECTED + '\'' +
                        ", refreshToken='" + DTOConsts.PROTECTED + '\'' +
                        ", is2fa=" + is2fa +
                        '}';
        }

        private TokenPairDTO() {}
}
