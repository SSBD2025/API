package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestResult;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.BloodTestConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class UpdateBloodTestReportDTO {
    @NotNull
    @NotBlank
    String lockToken;
    @NotNull
    @Size(min = BloodTestConsts.RESULTS_MIN_SIZE, max = BloodTestConsts.RESULTS_MAX_SIZE)
    List<BloodTestResult> results;

    public UpdateBloodTestReportDTO() {}

    @Override
    public String toString() {
        return "UpdateBloodTestReportDTO{" +
                "lockToken='" + DTOConsts.PROTECTED + '\'' +
                ", results=" + results +
                '}';
    }
}
