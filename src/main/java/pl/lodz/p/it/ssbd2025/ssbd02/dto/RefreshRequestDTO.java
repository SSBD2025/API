package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class RefreshRequestDTO {
        @NotBlank(groups = OnCreate.class)
        @Pattern(
                regexp = "^[A-Za-z0-9_-]{2,}(?:\\.[A-Za-z0-9_-]{2,}){2}$",
                message = "Invalid JWT format",
                groups = {OnCreate.class}
        )
        String refreshToken;

        private RefreshRequestDTO() {}

        @Override
        public String toString() {
                return "RefreshRequestDTO{" +
                        "refreshToken='" + DTOConsts.PROTECTED + '\'' +
                        '}';
        }
}
