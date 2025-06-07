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
//    String name;
//    String description;
//    String unit;
//    BigDecimal standardMin;
//    BigDecimal standardMax;
//
//    public BloodParameterDTO(BloodParameter parameter, boolean isMan) {
//        this.name = parameter.name();
//        this.description = parameter.getDescription();
//        this.unit = parameter.getUnit().toString();
//        if(isMan) {
//            this.standardMin = parameter.getMenStandardMin();
//            this.standardMax = parameter.getMenStandardMax();
//        } else {
//            this.standardMin = parameter.getWomanStandardMin();
//            this.standardMax = parameter.getWomanStandardMax();
//        }
//    }
//
//    public BloodParameterDTO() {
//    }
    String name;
    String description;
    String unit;
    BigDecimal womanStandardMin;
    BigDecimal womanStandardMax;
    BigDecimal menStandardMin;
    BigDecimal menStandardMax;
}
