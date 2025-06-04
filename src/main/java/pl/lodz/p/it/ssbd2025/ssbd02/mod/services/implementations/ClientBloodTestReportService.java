package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.UpdateBloodTestReportDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientBloodTestReport;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ClientBloodTestReportNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientBloodTestReportRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientBloodTestReportService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.util.List;
import java.util.UUID;

@TransactionLogged
@MethodCallLogged
@Component
@Service
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", timeoutString = "${transaction.timeout}")
public class ClientBloodTestReportService implements IClientBloodTestReportService {
    @NotNull
    private final ClientBloodTestReportRepository clientBloodTestReportRepository;
    @NotNull
    private final LockTokenService lockTokenService;
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

    @PreAuthorize("hasRole('DIETICIAN')")
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager" , readOnly = false, timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}"
    )
    public void updateReport(UpdateBloodTestReportDTO reportDTO) {
        LockTokenService.Record<UUID, Long> record = lockTokenService.verifyToken(reportDTO.getLockToken());
        ClientBloodTestReport report = clientBloodTestReportRepository.findById(record.id()).orElseThrow(ClientBloodTestReportNotFoundException::new);
        report.setResults(reportDTO.getResults());
        clientBloodTestReportRepository.saveAndFlush(report);
    }
}
