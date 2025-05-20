package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class UpdateAccountDTO {
        @NotNull
        @Size(max = AccountConsts.NAME_MAX)
        String firstName;

        @NotNull
        @Size(max = AccountConsts.NAME_MAX)
        String lastName;

        @NotNull
        String lockToken;

        private UpdateAccountDTO() {}

        @Override
        public String toString() {
                return "UpdateAccountDTO{" +
                        "firstName='" + firstName + '\'' +
                        ", lastName='" + lastName + '\'' +
                        ", lockToken='" + DTOConsts.PROTECTED + '\'' +
                        '}';
        }
}
