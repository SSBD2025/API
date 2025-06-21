package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class RoleChangeLogDTO {

        private String previousRole;

        @NotBlank(message = "New role must not be blank")
        @Size(min = 1, max = 100, message = "New role length must be between 1 and 100 characters")
        private String newRole;

        private RoleChangeLogDTO() {}

        @Override
        public String toString() {
                return "RoleChangeLogDTO{" +
                        "previousRole='" + previousRole + '\'' +
                        ", newRole='" + newRole + '\'' +
                        '}';
        }
}
