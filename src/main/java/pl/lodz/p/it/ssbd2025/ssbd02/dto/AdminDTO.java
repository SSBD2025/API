package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.Valid;
import lombok.*;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class AdminDTO{
        @Valid private UserRoleDTO.AdminDTO admin;
        @Valid AccountDTO account;

        private AdminDTO() {}

    @Override
    public String toString() {
        return "AdminDTO{" +
                "admin=" + admin +
                ", account=" + account +
                '}';
    }
}
