package pl.lodz.p.it.ssbd2025.ssbd02.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientBloodTestReport;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.BloodTestResultRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientBloodTestReportRepository;

import java.util.UUID;

@TestComponent
public class ClientBloodTestReportTestHelper {

    @Autowired
    private ClientBloodTestReportRepository reportRepo;

    @Autowired
    private BloodTestResultRepository resultRepo;

    @Autowired
    private ModTestHelper modTestHelper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "modTransactionManager")
    public ClientBloodTestReport getClientBloodTestReportById(UUID reportId) {
        modTestHelper.setDieticianContext();
        ClientBloodTestReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + reportId));
        modTestHelper.resetContext();
        report.getResults().size();
        report.getResults().forEach(result -> {
            if (result.getBloodParameter() != null) {
                result.getBloodParameter().name();
            }
        });

        return report;
    }
}