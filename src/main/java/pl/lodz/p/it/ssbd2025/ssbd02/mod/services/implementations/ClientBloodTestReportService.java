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
import org.springframework.validation.annotation.Validated;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodTestResultDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientBloodTestReportDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestResult;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientBloodTestReport;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.BloodTestResultNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.UpdateBloodTestReportDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ClientBloodTestReportNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientBloodTestReportRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientBloodTestReportService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@TransactionLogged
@MethodCallLogged
@Component
@Service
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
public class ClientBloodTestReportService implements IClientBloodTestReportService {
    @NotNull
    private final ClientBloodTestReportRepository clientBloodTestReportRepository;
    @NotNull
    private final LockTokenService lockTokenService;

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager", readOnly = true, timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Override
    public List<ClientBloodTestReportDTO> getAllByClientId(SensitiveDTO clientId) {
        UUID uuid = UUID.fromString(clientId.getValue());
        List<ClientBloodTestReport> reports = clientBloodTestReportRepository.findAllByClientId(uuid);
        List<ClientBloodTestReportDTO> dtos = new ArrayList<>();
        for (ClientBloodTestReport report : reports) {
            List<BloodTestResultDTO> resultsDTO = new ArrayList<>();
            for (BloodTestResult result : report.getResults()) {
                resultsDTO.add(new BloodTestResultDTO(lockTokenService.generateToken(result.getId(), result.getVersion()).getValue(), result.getResult(), result.getBloodParameter()));
            }
            dtos.add(new ClientBloodTestReportDTO(lockTokenService.generateToken(report.getId(), report.getVersion()).getValue(), resultsDTO, null));
        }
        return dtos;
    }

    @Override
    public ClientBloodTestReportDTO getById(SensitiveDTO reportId) {
        ClientBloodTestReport report = clientBloodTestReportRepository.findById(UUID.fromString(reportId.getValue())).orElseThrow(ClientBloodTestReportNotFoundException::new);
        List<BloodTestResultDTO> resultsDTO = new ArrayList<>();
        for (BloodTestResult result : report.getResults()) {
            resultsDTO.add(new BloodTestResultDTO(lockTokenService.generateToken(result.getId(), result.getVersion()).getValue(), result.getResult(), result.getBloodParameter()));
        }
        return new ClientBloodTestReportDTO(lockTokenService.generateToken(report.getId(), report.getVersion()).getValue(), resultsDTO, null);
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
    public void updateReport(@Validated(OnUpdate.class) ClientBloodTestReportDTO reportDTO) {
        LockTokenService.Record<UUID, Long> reportRecord = lockTokenService.verifyToken(reportDTO.getLockToken());
        ClientBloodTestReport report = clientBloodTestReportRepository
                .findById(reportRecord.id())
                .orElseThrow(ClientBloodTestReportNotFoundException::new);
        if(!Objects.equals(reportRecord.version(), report.getVersion())) {
            throw new ConcurrentUpdateException();
        }

        Map<UUID, BloodTestResult> existingResultsById = report.getResults().stream()
                .collect(Collectors.toMap(BloodTestResult::getId, Function.identity()));

        for (BloodTestResultDTO resultDTO : reportDTO.getResults()) {
            LockTokenService.Record<UUID, Long> resultRecord = lockTokenService.verifyToken(resultDTO.getLockToken());
            BloodTestResult existingResult = existingResultsById.get(resultRecord.id());
            if (existingResult == null) {
                throw new BloodTestResultNotFoundException();
            }
            if(!Objects.equals(existingResult.getVersion(), resultRecord.version())) {
                throw new ConcurrentUpdateException();
            }
            existingResult.setResult(resultDTO.getResult());
        }
        clientBloodTestReportRepository.saveAndFlush(report);
    }
}
