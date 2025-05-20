package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.Valid;
import lombok.*;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class DieticianDTO{
        @Valid UserRoleDTO.DieticianDTO dietician;
        @Valid AccountDTO account;

        private DieticianDTO(){}

    @Override
    public String toString() {
        return "DieticianDTO{" +
                "dietician=" + dietician +
                ", account=" + account +
                '}';
    }
}
