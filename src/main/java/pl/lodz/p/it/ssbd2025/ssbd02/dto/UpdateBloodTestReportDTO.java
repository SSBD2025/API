package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestResult;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class UpdateBloodTestReportDTO {
    @NotNull
    String lockToken;
    @NotNull
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
