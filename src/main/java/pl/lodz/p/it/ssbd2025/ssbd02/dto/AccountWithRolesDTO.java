package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class AccountWithRolesDTO {
    @NotNull
    AccountDTO accountDTO;
    @NotNull
    List<AccountRoleDTO> userRoleDTOS;

    private AccountWithRolesDTO() {
    }

    @Override
    public String toString() {
        return "AccountWithRolesDTO{" +
                "accountDTO=" + accountDTO +
                ", userRoleDTOS=" + userRoleDTOS +
                '}';
    }
}
