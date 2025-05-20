package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class AccountRoleDTO{
        @NotNull
        String roleName;
        @NotNull
        Boolean active;

        private AccountRoleDTO(){}

        @Override
        public String toString() {
                return "AccountRoleDTO{" +
                        "roleName='" + roleName + '\'' +
                        ", active=" + active +
                        '}';
        }
}
