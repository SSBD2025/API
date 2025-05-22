package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientBloodTestReport;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientBloodTestReportService;

import java.util.List;
import java.util.UUID;

public class ClientBloodTestReportService implements IClientBloodTestReportService {
    @Override
    public List<ClientBloodTestReport> getAllByClientId(UUID clientId) {
        return List.of();
    }

    @Override
    public ClientBloodTestReport getById(UUID reportId) {
        return null;
    }

    @Override
    public ClientBloodTestReport createReport(UUID clientId, ClientBloodTestReport report) {
        return null;
    }

    @Override
    public void deleteReport(UUID reportId) {

    }
}
