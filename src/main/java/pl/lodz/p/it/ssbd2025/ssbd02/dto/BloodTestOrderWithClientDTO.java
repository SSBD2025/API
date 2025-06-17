package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import lombok.*;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class BloodTestOrderWithClientDTO {

    BloodTestOrderDTO bloodTestOrderDTO;
    MinimalClientDTO minimalClientDTO;

    @Override
    public String toString() {
        return "BloodTestOrderWithClientDTO {"
                + "bloodTestOrder=" + bloodTestOrderDTO
                + ", client=" + minimalClientDTO
                + "}";
    }
}
