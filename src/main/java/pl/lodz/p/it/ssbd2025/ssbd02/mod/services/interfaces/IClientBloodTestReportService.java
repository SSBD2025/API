package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.UpdateBloodTestReportDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientBloodTestReport;

import java.util.List;
import java.util.UUID;

public interface IClientBloodTestReportService {
    List<ClientBloodTestReport> getAllByClientId(UUID clientId);
    ClientBloodTestReport getById(UUID reportId);
    ClientBloodTestReport createReport(UUID clientId, ClientBloodTestReport report);
    void deleteReport(UUID reportId);
    void updateReport(UpdateBloodTestReportDTO reportDTO);
}
