package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class AccountWithTokenDTO {

        @NotNull(message = AccountConsts.ACCOUNT_NOT_NULL_MESSAGE)
        @Valid
        private AccountDTO account;

        private String lockToken;

        @NotNull(message = AccountConsts.ROLES_NOT_NULL_MESSAGE)
        @NotEmpty(message = AccountConsts.ROLES_NOT_EMPTY_MESSAGE)
        @Valid
        private Iterable<@Valid AccountRoleDTO> roles;

        private AccountWithTokenDTO() {}

        @Override
        public String toString() {
                return "AccountWithTokenDTO{" +
                        "account=" + account +
                        ", lockTokenl='" + pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts.PROTECTED + '\'' +
                        ", roles=" + roles +
                        '}';
        }
}
