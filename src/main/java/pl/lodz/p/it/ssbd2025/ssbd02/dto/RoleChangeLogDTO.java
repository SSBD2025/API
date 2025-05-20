package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class RoleChangeLogDTO {
        String previousRole;
        @NotNull
        String newRole;

        private RoleChangeLogDTO() {}

        @Override
        public String toString() {
                return "RoleChangeLogDTO{" +
                        "previousRole='" + previousRole + '\'' +
                        ", newRole='" + newRole + '\'' +
                        '}';
        }
}
