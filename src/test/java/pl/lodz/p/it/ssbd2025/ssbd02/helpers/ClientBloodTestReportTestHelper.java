package pl.lodz.p.it.ssbd2025.ssbd02.helpers;

import org.springframework.beans.factory.annotation.Autowired;
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

    // Metoda do ustawiania PLT - używa nowej transakcji aby zmiana została commitowana
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
    public void setPLTValueTo250(UUID reportId) {
        ClientBloodTestReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        // Inicjalizujemy kolekcję
        report.getResults().size();

        // Znajdujemy wynik o parametrze PLT
        BloodTestResult pltResult = report.getResults().stream()
                .filter(result -> "PLT".equals(result.getBloodParameter().name()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("PLT result not found in report"));

        pltResult.setResult(250.0);
        reportRepo.saveAndFlush(report);

        System.out.println("PLT value restored to 250.0 for report: " + reportId);
    }

    // Metoda do odczytu raportu - używa nowej transakcji aby zagwarantować dostęp do lazy collections
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "modTransactionManager")
    public ClientBloodTestReport getClientBloodTestReportById(UUID reportId) {
        // Jeśli masz metodę findByIdWithResults w repository, użyj jej zamiast findById
        // ClientBloodTestReport report = reportRepo.findByIdWithResults(reportId)
        //         .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + reportId));

        ClientBloodTestReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + reportId));

        // Inicjalizujemy kolekcję wyników aby uniknąć lazy loading exception
        report.getResults().size();
        // Dodatkowo inicjalizujemy nested properties
        report.getResults().forEach(result -> {
            if (result.getBloodParameter() != null) {
                result.getBloodParameter().name(); // Inicjalizujemy blood parameter
            }
        });

        return report;
    }

    // Metoda do odczytu wartości PLT - używa nowej transakcji read-only aby zawsze czytać aktualny stan
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "modTransactionManager")
    public Double getPLTValue(UUID reportId) {
        ClientBloodTestReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        // Inicjalizujemy kolekcję
        report.getResults().size();

        return report.getResults().stream()
                .filter(result -> "PLT".equals(result.getBloodParameter().name()))
                .findFirst()
                .map(BloodTestResult::getResult)
                .orElseThrow(() -> new IllegalStateException("PLT result not found in report"));
    }

    // Metoda do przywracania stanu - używa nowej transakcji i WYMUSZA ustawienie wartości
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
    public void restorePLTValueTo250(UUID reportId) {
        ClientBloodTestReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        // Inicjalizujemy kolekcję
        report.getResults().size();

        BloodTestResult pltResult = report.getResults().stream()
                .filter(result -> "PLT".equals(result.getBloodParameter().name()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("PLT result not found in report"));

        // ZAWSZE ustawiamy wartość na 250.0, niezależnie od aktualnej wartości
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
        ClientBloodTestReport report = reportRepo.findById(id).orElseThrow();
        // Inicjalizujemy kolekcję wyników aby uniknąć lazy loading exception
        report.getResults().size();
        return report;
    }

    // Dodatkowa metoda pomocnicza do weryfikacji stanu
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

    // Metoda do bezpośredniego update'u wartości PLT przez JPQL - bardziej niezawodna
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
    public void forcePLTValueTo250(UUID reportId) {
        // Najpierw sprawdzamy czy raport istnieje
        ClientBloodTestReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        // Inicjalizujemy kolekcję aby upewnić się że PLT result istnieje
        report.getResults().size();

        BloodTestResult pltResult = report.getResults().stream()
                .filter(result -> "PLT".equals(result.getBloodParameter().name()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("PLT result not found in report"));

        // Bezpośrednio ustawiamy wartość i zapisujemy
        Double oldValue = pltResult.getResult();
        pltResult.setResult(250.0);

        // Używamy merge zamiast save aby upewnić się że zmiana zostanie zapisana
        BloodTestResult savedResult = resultRepo.saveAndFlush(pltResult);

        System.out.println("PLT value FORCED from " + oldValue + " to " + savedResult.getResult() + " for report: " + reportId);

        // Dodatkowa weryfikacja
        Double verificationValue = getPLTValue(reportId);
        if (!verificationValue.equals(250.0)) {
            throw new IllegalStateException("Failed to set PLT value to 250.0. Current value: " + verificationValue);
        }
    }
}