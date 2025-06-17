package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AssignDietPlanDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodTestOrderDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.PeriodicSurveyDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.BloodTestOrderMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.PeriodicSurveyMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.*;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.*;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.*;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IDieticianService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@TransactionLogged
@Component
@RequiredArgsConstructor
@Service
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
public class DieticianModService implements IDieticianService {
    private final ClientModRepository clientModRepository;
    private final FoodPyramidRepository foodPyramidRepository;
    private final ClientFoodPyramidRepository clientFoodPyramidRepository;
    private final DieticianModRepository dieticianModRepository;
    private final SurveyRepository surveyRepository;
    private final BloodTestOrderRepository bloodTestOrderRepository;
    private final BloodTestOrderMapper bloodTestOrderMapper;
    private final EmailService emailService;
    private final PeriodicSurveyRepository periodicSurveyRepository;
    private final PeriodicSurveyMapper periodicSurveyMapper;

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW, readOnly = true,
            transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('DIETICIAN')")
    public List<Client> getClientsByDietician(String searchPhrase) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Dietician dietician = dieticianModRepository.findByLogin(login).orElseThrow(DieticianNotFoundException::new);
        if (searchPhrase != null && !searchPhrase.isEmpty()) {
            return clientModRepository.findByDieticianIdAndSearchPhrase(dietician.getId(), searchPhrase);
        }
        return clientModRepository.findByDieticianId(dietician.getId());
    }

    @Override
    public Dietician getById(UUID id) {
        return null;
    }

    @Override
    public List<Client> getClients(UUID dieticianId) {
        return List.of();
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            timeoutString = "${transaction.timeout}"
    )
    @PreAuthorize("hasRole('DIETICIAN')")
    public Survey getPermanentSurveyByClientId(UUID clientId) {
        return surveyRepository.findByClientId(clientId).orElseThrow(SurveyNotFoundException::new);
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            timeoutString = "${transaction.timeout}"
    )
    @PreAuthorize("hasRole('DIETICIAN')")
    public Client getClientDetails(UUID clientId) {
        Client client = clientModRepository.findClientById(clientId).orElseThrow(ClientNotFoundException::new);
        if (client.getSurvey() != null) {
            client.getSurvey().getDietPreferences().size();
            client.getSurvey().getAllergies().size();
            client.getSurvey().getIllnesses().size();
            client.getSurvey().getMedications().size();
            client.getSurvey().getMealTimes().size();
        }
        return client;
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW, readOnly = false,
            transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasRole('DIETICIAN')")
    public BloodTestOrder orderMedicalExaminations(BloodTestOrderDTO bloodTestOrderDTO) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Dietician dietician = dieticianModRepository.findByLogin(login)
                .orElseThrow(DieticianNotFoundException::new);
        Client client = clientModRepository.findClientById(bloodTestOrderDTO.getClientId())
                .orElseThrow(ClientNotFoundException::new);
        if (client.getDietician() == null) {
            throw new ClientHasNoAssignedDieticianException();
        }
        if (!client.getDietician().equals(dietician)) {
            throw new DieticianAccessDeniedException();
        }
        if (bloodTestOrderRepository.hasUnfulfilledOrders(client)) {
            throw new BloodTestAlreadyOrderedException();
        }
        BloodTestOrder bloodTestOrder = bloodTestOrderMapper.toBloodTestOrder(bloodTestOrderDTO);
        bloodTestOrder.setOrderDate(Timestamp.from(Instant.now()));
        bloodTestOrder.setClient(client);
        bloodTestOrder.setDietician(dietician);
        BloodTestOrder savedBloodTestOrder = bloodTestOrderRepository.saveAndFlush(bloodTestOrder);
        emailService.sendBloodTestOrderNotificationEmail(client.getAccount().getLogin(), client.getAccount().getEmail(), client.getAccount().getLanguage());
        return savedBloodTestOrder;
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            timeoutString = "${transaction.timeout}"
    )
    @PreAuthorize("hasRole('DIETICIAN')")
    public List<BloodTestOrder> getUnfulfilledBloodTestOrders() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Dietician dietician = dieticianModRepository.findByLogin(login).orElseThrow(DieticianNotFoundException::new);
        return bloodTestOrderRepository.getAllByDietician_IdAndFulfilled(dietician.getId(), false);
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW, readOnly = false,
            transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('DIETICIAN')")
    public void confirmBloodTestOrder(UUID orderId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Dietician dietician = dieticianModRepository.findByLogin(login)
                .orElseThrow(DieticianNotFoundException::new);
        BloodTestOrder bloodTestOrder = bloodTestOrderRepository.findById(orderId)
                .orElseThrow(BloodTestOrderNotFoundException::new);
        if (!bloodTestOrder.getDietician().equals(dietician)) {
            throw new DieticianAccessDeniedException();
        }
        if (bloodTestOrder.isFulfilled()) {
            throw new BloodTestOrderAlreadyFulfilledException();
        }
        bloodTestOrder.setFulfilled(true);
        bloodTestOrderRepository.saveAndFlush(bloodTestOrder);
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = true,
            timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {
            JpaSystemException.class},
            backoff = @Backoff(
                    delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasRole('DIETICIAN')")
    public Page<PeriodicSurveyDTO> getPeriodicSurveysByAccountId(
            UUID accountId,
            Pageable pageable,
            @Nullable Timestamp since,
            @Nullable Timestamp before
    ) {
        Client client = clientModRepository.findClientById(accountId).orElseThrow(ClientNotFoundException::new);
        Page<PeriodicSurvey> surveysPage;
        if (since != null && before != null) {
            surveysPage = periodicSurveyRepository.findByClientIdAndMeasurementDateBetween(
                    client.getId(), since, before, pageable);
        } else if (since != null) {
            surveysPage = periodicSurveyRepository.findByClientIdAndMeasurementDateAfter(
                    client.getId(), since, pageable);
        } else if (before != null) {
            surveysPage = periodicSurveyRepository.findByClientIdAndMeasurementDateBefore(
                    client.getId(), before, pageable);
        } else {
            surveysPage = periodicSurveyRepository.findByClientId(client.getId(), pageable);
        }
        if(surveysPage == null || surveysPage.isEmpty()) throw new PeriodicSurveyNotFound();
        return surveysPage.map(periodicSurveyMapper::toPeriodicSurveyDTO);
    }
}
