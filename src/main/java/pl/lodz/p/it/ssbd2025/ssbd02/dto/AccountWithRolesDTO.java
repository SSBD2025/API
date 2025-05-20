package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import lombok.*;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class AccountWithRolesDTO {
        AccountDTO accountDTO;
        List<AccountRoleDTO> userRoleDTOS;

    private AccountWithRolesDTO() {}

    @Override
    public String toString() {
        return "AccountWithRolesDTO{" +
                "accountDTO=" + accountDTO +
                ", userRoleDTOS=" + userRoleDTOS +
                '}';
    }
}
