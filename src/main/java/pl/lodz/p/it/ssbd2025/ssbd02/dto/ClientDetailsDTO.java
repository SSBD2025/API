package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

import java.util.List;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClientDetailsDTO {

    @NotBlank(groups = {OnCreate.class, OnRead.class})
    @Size(min = AccountConsts.NAME_MIN, max = AccountConsts.NAME_MAX, groups = {OnCreate.class, OnUpdate.class})
    private String firstName;

    @NotBlank(groups = {OnCreate.class, OnRead.class})
    @Size(min = AccountConsts.NAME_MIN, max = AccountConsts.NAME_MAX, groups = {OnCreate.class, OnUpdate.class})
    private String lastName;

    @Valid
    private SurveyDTO survey;

    @Valid
    private List<PeriodicSurveyDTO> periodicSurvey;

    @Valid
    private List<ClientBloodTestReportDTO> bloodTestReport;

    @Override
    public String toString() {
        return "CompleteClientDataDTO{" +
                ", permanentSurvey=" + survey +
                ", periodicSurveys=" + DTOConsts.PROTECTED +
                ", bloodTestReports=" + DTOConsts.PROTECTED +
                '}';
    }
}
