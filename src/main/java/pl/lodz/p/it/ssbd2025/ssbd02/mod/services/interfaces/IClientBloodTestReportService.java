package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import org.springframework.validation.annotation.Validated;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientBloodTestReportDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.UpdateBloodTestReportDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientBloodTestReport;

import java.util.List;
import java.util.UUID;

public interface IClientBloodTestReportService {
    List<ClientBloodTestReportDTO> getAllByClientId(SensitiveDTO clientId);
    List<ClientBloodTestReportDTO> getAllByClientLogin();
    ClientBloodTestReportDTO getById(SensitiveDTO reportId);
    ClientBloodTestReport createReport(SensitiveDTO clientId, ClientBloodTestReport report);
    void deleteReport(UUID reportId);
    void updateReport(@Validated(OnUpdate.class) ClientBloodTestReportDTO reportDTO);
}
