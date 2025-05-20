package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import lombok.*;
import org.springframework.validation.annotation.Validated;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRequest;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

import java.util.Collection;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class AccountWithTokenDTO{
        //@Validated(OnRequest.class) ?? todo co z tym
        AccountDTO account;
        String lockToken;
        Iterable<AccountRoleDTO> roles;

        private AccountWithTokenDTO() {}

        @Override
        public String toString() {
            return "AccountWithTokenDTO{" +
                    "account=" + account +
                    ", lockTokenl='" + DTOConsts.PROTECTED + '\'' +
                    ", roles=" + roles +
                    '}';
        }
}
