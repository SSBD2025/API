package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.BloodParameter;

import java.math.BigDecimal;

@Data
@Getter
@Setter(AccessLevel.NONE)
@AllArgsConstructor
@NoArgsConstructor
public class BloodParameterDTO {

    String name;
    String description;
    String unit;
    BigDecimal standardMin;
    BigDecimal standardMax;
}
