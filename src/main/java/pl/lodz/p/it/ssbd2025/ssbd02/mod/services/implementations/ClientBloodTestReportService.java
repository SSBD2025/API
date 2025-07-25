package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.BloodParameterMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.BloodTestResultMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.ClientBloodTestReportMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.*;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.*;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.BloodTestOrderRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientBloodTestReportRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientModRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.DieticianModRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientBloodTestReportService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.sql.Timestamp;
import java.time.Instant;
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
    @NotNull
    private final ClientModRepository clientModRepository;
    @NotNull
    private final ClientModService clientModService;
    @NotNull
    private final BloodParameterMapper bloodParameterMapper;
    private final BloodTestResultMapper bloodTestResultMapper;
    private final ClientBloodTestReportMapper clientBloodTestReportMapper;
    private final DieticianModRepository dieticianModRepository;
    private final BloodTestOrderRepository bloodTestOrderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager", readOnly = true, timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Override
    public List<ClientBloodTestReportDTO> getAllByClientId(SensitiveDTO clientId) {
        return getClientBloodTestReportDTOS(UUID.fromString(clientId.getValue()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager", readOnly = true, timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Override
    public List<ClientBloodTestReportDTO> getAllByClientLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        UUID uuid = clientModService.getClientByLogin(new SensitiveDTO(login)).getId();
        return getClientBloodTestReportDTOS(uuid);
    }

    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    @NotNull
    private List<ClientBloodTestReportDTO> getClientBloodTestReportDTOS(UUID uuid) {
        List<ClientBloodTestReport> reports = clientBloodTestReportRepository.findAllByClientId(uuid);
        if (reports.isEmpty()) {
            throw new ClientBloodTestReportNotFoundException();
        }
        Client client = clientModRepository.findClientById(uuid).orElseThrow(ClientNotFoundException::new);
        if (client.getSurvey() == null) {
            throw new PermanentSurveyNotFoundException();
        }
        List<ClientBloodTestReportDTO> dtos = new ArrayList<>();
        for (ClientBloodTestReport report : reports) {
            List<BloodTestResultDTO> resultsDTO = new ArrayList<>();
            for (BloodTestResult result : report.getResults()) {
                BloodParameterDTO bloodParameterDTO = bloodParameterMapper.toBloodParameterDTO(result.getBloodParameter(), client.getSurvey().isGender());
                BloodTestResultDTO bloodTestResultDTO = bloodTestResultMapper.toBloodTestResultDTO(result, client.getSurvey().isGender());
                bloodTestResultDTO.setBloodParameter(bloodParameterDTO);
                resultsDTO.add(bloodTestResultDTO);
            }
            dtos.add(new ClientBloodTestReportDTO(null, null, null, report.getTimestamp(), resultsDTO, lockTokenService.generateToken(report.getId(), report.getVersion()).getValue()));
        }
        return dtos;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager", readOnly = true, timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('DIETICIAN')")
    @Override
    public ClientBloodTestReportDTO getById(SensitiveDTO reportId) {
        ClientBloodTestReport report = clientBloodTestReportRepository.findById(UUID.fromString(reportId.getValue())).orElseThrow(ClientBloodTestReportNotFoundException::new);
        List<BloodTestResultDTO> resultsDTO = new ArrayList<>();
        if(clientModService.getClientById(report.getClient().getId()).getSurvey() == null) {
            throw new PermanentSurveyNotFoundException();
        }
        for (BloodTestResult result : report.getResults()) {
            BloodParameterDTO bloodParameterDTO = bloodParameterMapper.toBloodParameterDTO(result.getBloodParameter(), clientModService.getClientById(report.getClient().getId()).getSurvey().isGender());
            BloodTestResultDTO bloodTestResultDTO = bloodTestResultMapper.toBloodTestResultDTO(result, clientModService.getClientById(report.getClient().getId()).getSurvey().isGender());
            bloodTestResultDTO.setBloodParameter(bloodParameterDTO);
            resultsDTO.add(bloodTestResultDTO);
        }
        return new ClientBloodTestReportDTO(null, null, null, report.getTimestamp(), resultsDTO, lockTokenService.generateToken(report.getId(), report.getVersion()).getValue());
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = false,
            timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}"
    )
    @PreAuthorize("hasRole('DIETICIAN')")
    public ClientBloodTestReportDTO createReport(SensitiveDTO clientId, ClientBloodTestReport report) {
        Client client = clientModRepository.findClientById(UUID.fromString(clientId.getValue())).orElseThrow(ClientNotFoundException::new);
        report.setClient(client);
        report.setTimestamp(Timestamp.from(Instant.now()));
        report.getResults().forEach(result -> result.setReport(report));
        Survey survey = client.getSurvey();
        if(survey == null) {
            throw new PermanentSurveyNotFoundException();
        }
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Dietician dietician = dieticianModRepository.findByLogin(login)
                .orElseThrow(DieticianNotFoundException::new);
        BloodTestOrder bloodTestOrder = bloodTestOrderRepository.findTopByClient_IdAndFulfilledFalseOrderByOrderDateDesc(client.getId())
                .orElseThrow(BloodTestOrderNotFoundException::new);
        if (!bloodTestOrder.getDietician().equals(dietician)) {
            throw new DieticianAccessDeniedException();
        }
        if (bloodTestOrder.isFulfilled()) {
            throw new BloodTestOrderAlreadyFulfilledException();
        }
        bloodTestOrder.setFulfilled(true);
        bloodTestOrderRepository.saveAndFlush(bloodTestOrder);
        return clientBloodTestReportMapper.toClientBloodTestReportDTO(clientBloodTestReportRepository.saveAndFlush(report), survey.isGender());
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
