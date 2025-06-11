package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;

import java.util.List;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClientDetailsDTO {

    private String firstName;

    private String lastName;

    private SurveyDTO survey;

    private List<PeriodicSurveyDTO> periodicSurvey;

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
