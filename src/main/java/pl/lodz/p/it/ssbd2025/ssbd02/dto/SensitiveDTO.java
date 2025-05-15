package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import lombok.*;

@Data
@ToString
@Getter
@AllArgsConstructor
public class SensitiveDTO {
    @ToString.Exclude
    private String value;

    private SensitiveDTO(){}
}
