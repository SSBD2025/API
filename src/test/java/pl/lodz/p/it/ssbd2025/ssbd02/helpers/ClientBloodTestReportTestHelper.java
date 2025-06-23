package pl.lodz.p.it.ssbd2025.ssbd02.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestResult;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientBloodTestReport;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.BloodTestResultRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientBloodTestReportRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientModRepository;

import java.util.List;
import java.util.UUID;

@TestComponent
public class ClientBloodTestReportTestHelper {

    @Autowired
    private ClientBloodTestReportRepository reportRepo;

    @Autowired
    private ClientModRepository clientRepo;

    @Autowired
    private BloodTestResultRepository resultRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
    public void setPLTValueTo250(UUID reportId) {
        ClientBloodTestReport report = reportRepo.findByIdForTest(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.getResults().size();

        BloodTestResult pltResult = report.getResults().stream()
                .filter(result -> "PLT".equals(result.getBloodParameter().name()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("PLT result not found in report"));

        pltResult.setResult(250.0);
        reportRepo.saveAndFlush(report);

        System.out.println("PLT value restored to 250.0 for report: " + reportId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "modTransactionManager")
    public ClientBloodTestReport getClientBloodTestReportById(UUID reportId) {
        ClientBloodTestReport report = reportRepo.findByIdForTest(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + reportId));

        report.getResults().size();
        report.getResults().forEach(result -> {
            if (result.getBloodParameter() != null) {
                result.getBloodParameter().name();
            }
        });

        return report;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "modTransactionManager")
    public Double getPLTValue(UUID reportId) {
        ClientBloodTestReport report = reportRepo.findByIdForTest(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.getResults().size();

        return report.getResults().stream()
                .filter(result -> "PLT".equals(result.getBloodParameter().name()))
                .findFirst()
                .map(BloodTestResult::getResult)
                .orElseThrow(() -> new IllegalStateException("PLT result not found in report"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
    public void restorePLTValueTo250(UUID reportId) {
        ClientBloodTestReport report = reportRepo.findByIdForTest(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.getResults().size();

        BloodTestResult pltResult = report.getResults().stream()
                .filter(result -> "PLT".equals(result.getBloodParameter().name()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("PLT result not found in report"));

        Double currentValue = pltResult.getResult();
        pltResult.setResult(250.0);
        reportRepo.saveAndFlush(report);

        System.out.println("PLT value changed from " + currentValue + " to 250.0 for report: " + reportId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
    public void removeAllReports(UUID clientId) {
        List<ClientBloodTestReport> all = reportRepo.findAllByClientId(clientId);
        reportRepo.deleteAllInBatch(all);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "modTransactionManager")
    public ClientBloodTestReport getReport(UUID id) {
        ClientBloodTestReport report = reportRepo.findByIdForTest(id).orElseThrow();
        report.getResults().size();
        return report;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "modTransactionManager")
    public void verifyPLTValue(UUID reportId, Double expectedValue) {
        Double actualValue = getPLTValue(reportId);
        if (!actualValue.equals(expectedValue)) {
            throw new IllegalStateException(
                    String.format("PLT value mismatch for report %s. Expected: %s, Actual: %s",
                            reportId, expectedValue, actualValue)
            );
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
    public void forcePLTValueTo250(UUID reportId) {
        ClientBloodTestReport report = reportRepo.findByIdForTest(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.getResults().size();

        BloodTestResult pltResult = report.getResults().stream()
                .filter(result -> "PLT".equals(result.getBloodParameter().name()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("PLT result not found in report"));

        Double oldValue = pltResult.getResult();
        pltResult.setResult(250.0);

        BloodTestResult savedResult = resultRepo.saveAndFlush(pltResult);

        System.out.println("PLT value FORCED from " + oldValue + " to " + savedResult.getResult() + " for report: " + reportId);

        Double verificationValue = getPLTValue(reportId);
        if (!verificationValue.equals(250.0)) {
            throw new IllegalStateException("Failed to set PLT value to 250.0. Current value: " + verificationValue);
        }
    }
}