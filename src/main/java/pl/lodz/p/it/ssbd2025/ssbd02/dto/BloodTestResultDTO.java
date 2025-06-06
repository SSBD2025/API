package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.BloodParameter;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class BloodTestResultDTO {
    @NotNull
    String lockToken;
    @NotNull
    String result;
    @NotNull
    BloodParameterDTO bloodParameter;

    public BloodTestResultDTO() {}

    @Override
    public String toString() {
        return "BloodTestResultDTO{" +
                "lockToken='" + DTOConsts.PROTECTED + '\'' +
                ", result='" + result + '\'' +
                ", bloodParameter=" + bloodParameter +
                '}';
    }
}
