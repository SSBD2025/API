package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class ChangeEmailDTO {
        @Email
        @Size(min = AccountConsts.EMAIL_MIN, max = AccountConsts.EMAIL_MAX)
        @NotBlank
        String email;

        private ChangeEmailDTO(){}

        @Override
        public String toString() {
                return "ChangeEmailDTO{" +
                        "email='" + email + '\'' +
                        '}';
        }
}
