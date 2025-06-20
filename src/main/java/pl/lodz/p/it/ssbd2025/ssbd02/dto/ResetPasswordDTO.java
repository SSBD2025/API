package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRequest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnReset;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class ResetPasswordDTO {
        @Email(groups = {OnRequest.class, OnReset.class})
        @NotNull(groups = OnRequest.class)
        @NotBlank(groups = OnRequest.class)
        @Null(groups = OnReset.class)
        String email;
        @Null(groups = OnRequest.class)
        @NotNull(groups = OnReset.class)
        @NotBlank(groups = OnReset.class)
        String password;

        private ResetPasswordDTO() {}

        @Override
        public String toString() {
                return "ResetPasswordDTO{" +
                        "email='" + email + '\'' +
                        ", password='" + DTOConsts.PROTECTED + '\'' +
                        '}';
        }
}
