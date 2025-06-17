package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientStatusDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodTestOrderDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.PeriodicSurveyDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SurveyDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.BloodTestOrderMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.PeriodicSurveyMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.*;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ClientNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.NoAssignedDieticianException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.PermanentSurveyAlreadyExistsException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.*;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.*;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@TransactionLogged
@Component
@RequiredArgsConstructor
@Service
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
public class ClientModService implements IClientService {

    private final SurveyRepository surveyRepository;
    private final ClientModRepository clientModRepository;
    private final DieticianModRepository dieticianModRepository;
    private final PeriodicSurveyRepository periodicSurveyRepository;
    private final LockTokenService lockTokenService;
    private final PeriodicSurveyMapper periodicSurveyMapper;
    private final BloodTestOrderRepository bloodTestOrderRepository;
    private final BloodTestOrderMapper bloodTestOrderMapper;

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = true,
            timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class},
            backoff = @Backoff(
                    delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasRole('DIETICIAN')")
    public Client getClientById(UUID id) {
        return clientModRepository.findClientById(id).orElseThrow(ClientNotFoundException::new);
    }

    @Override
    public UUID getClientByAccountId(UUID id) {
        return clientModRepository.findUserIdByClientId(id).orElseThrow(AccountNotFoundException::new);
    }


    @Override
    public Client getClientByLogin(SensitiveDTO login) {
        return clientModRepository.findByLogin(login.getValue()).orElseThrow(AccountNotFoundException::new);
    }

    @Override
    @PreAuthorize("hasRole('CLIENT')")
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = false,
            timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class},
            backoff = @Backoff(
                    delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    public void assignDietician(UUID dieticianId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientModRepository.findByLogin(login).orElseThrow(ClientNotFoundException::new);
        Dietician dietician = dieticianModRepository.findDieticianById(dieticianId)
                .orElseThrow(DieticianNotFoundException::new);
        if (client.getDietician() != null) {
            if (client.getDietician().equals(dietician)) {
                throw new SameDieticianAlreadyAssignedException();
            }
            throw new DieticianAlreadyAssignedException();
        }
        if (dietician.getClients().size() >= 10) {
            throw new DieticianClientLimitExceededException();
        }
        client.setDietician(dietician);
        clientModRepository.saveAndFlush(client);
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW, readOnly = true,
            transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('CLIENT')")
    public ClientStatusDTO getClientStatus() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientModRepository.findByLogin(login).orElseThrow(ClientNotFoundException::new);
        ClientStatusDTO clientStatusDTO = new ClientStatusDTO();
        clientStatusDTO.setHasAssignedDietician(client.getDietician() != null);
        clientStatusDTO.setHasSubmittedPermanentSurvey(client.getSurvey() != null);
        return clientStatusDTO;
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = false,
            timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class},
            backoff = @Backoff(
                    delayExpression = "${app.retry.backoff}"),
                    maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasRole('CLIENT')")
    public Survey submitPermanentSurvey(Survey newSurvey) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientModRepository.findByLogin(login).orElseThrow(ClientNotFoundException::new);

        if (client.getDietician() == null) {
            throw new NoAssignedDieticianException();
        }

        if (surveyRepository.existsByClientId(client.getId())) {
            throw new PermanentSurveyAlreadyExistsException();
        }

        newSurvey.setClient(client);
        return surveyRepository.saveAndFlush(newSurvey);
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = true,
            timeoutString = "${transaction.timeout}")
    @Retryable(retryFor = {
            JpaSystemException.class,
            ConcurrentUpdateException.class},
    backoff = @Backoff(
            delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasRole('CLIENT')")
    public Survey getPermanentSurvey() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientModRepository.findByLogin(login).orElseThrow(ClientNotFoundException::new);
        return surveyRepository.findByClientId(client.getId()).orElseThrow(SurveyNotFoundException::new);
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW, readOnly = true,
            transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('CLIENT')")
    public List<Dietician> getAvailableDieticians(String searchPhrase) {
        if (searchPhrase != null && !searchPhrase.isEmpty()) {
            return dieticianModRepository.getAllAvailableDieticiansBySearchPhrase(searchPhrase);
        }
        return dieticianModRepository.getAllAvailableDietians();
    }

    @Override
    @PreAuthorize("hasRole('CLIENT')")
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = false,
            timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    public PeriodicSurvey submitPeriodicSurvey(PeriodicSurvey periodicSurvey) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientModRepository.findByLogin(login).orElseThrow(ClientNotFoundException::new);
        periodicSurvey.setClient(client);

        if (periodicSurveyRepository.existsByClientAndMeasurementDateAfter(periodicSurvey.getClient(),
                Timestamp.valueOf(LocalDateTime.now().minusHours(24)))) {
            throw new PeriodicSurveyTooSoonException();
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        periodicSurvey.setMeasurementDate(now);
        return periodicSurveyRepository.saveAndFlush(periodicSurvey);
    }

    @Override
    @PreAuthorize("hasRole('CLIENT')")
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = false,
            timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    public Survey editPermanentSurvey(SurveyDTO dto) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientModRepository.findByLogin(login).orElseThrow(ClientNotFoundException::new);

        Survey existingSurvey = surveyRepository.findByClientId(client.getId())
                .orElseThrow(PermanentSurveyNotFoundException::new);

        LockTokenService.Record<UUID, Long> record = lockTokenService.verifyToken(dto.getLockToken());

        if (!existingSurvey.getId().equals(record.id())) {
            throw new InvalidLockTokenException();
        }

        if (!existingSurvey.getVersion().equals(record.version())) {
            throw new ConcurrentUpdateException();
        }

        existingSurvey.setHeight(dto.getHeight());
        existingSurvey.setDietPreferences(dto.getDietPreferences());
        existingSurvey.setAllergies(dto.getAllergies());
        existingSurvey.setActivityLevel(dto.getActivityLevel());
        existingSurvey.setSmokes(dto.isSmokes());
        existingSurvey.setDrinksAlcohol(dto.isDrinksAlcohol());
        existingSurvey.setIllnesses(dto.getIllnesses());
        existingSurvey.setMedications(dto.getMedications());
        existingSurvey.setMealsPerDay(dto.getMealsPerDay());
        existingSurvey.setNutritionGoal(dto.getNutritionGoal());
        existingSurvey.setMealTimes(dto.getMealTimes());
        existingSurvey.setEatingHabits(dto.getEatingHabits());

        return surveyRepository.saveAndFlush(existingSurvey);
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
    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    public Page<PeriodicSurveyDTO> getPeriodicSurveys(UUID clientId, Pageable pageable) {
        Page<PeriodicSurvey> surveysPage = periodicSurveyRepository.findByClientId(clientId, pageable);
        return surveysPage.map(periodicSurveyMapper::toPeriodicSurveyDTO);
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
    @PreAuthorize("hasRole('CLIENT') || hasRole('DIETICIAN')")
    public PeriodicSurveyDTO getPeriodicSurvey(UUID periodicSurveyId) {
        return periodicSurveyMapper.toPeriodicSurveyDTO(periodicSurveyRepository.findById(periodicSurveyId)
                .orElseThrow(PeriodicSurveyNotFound::new));
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
    @PreAuthorize("hasRole('CLIENT')") // TO MA ZOSTAC CLIENT ONLY PRZESTAC MI TO ZMIENIAC !!!!!!!!!!!!!!!!!
    public Page<PeriodicSurveyDTO> getPeriodicSurveys(
            Pageable pageable,
            @Nullable Timestamp since,
            @Nullable Timestamp before
    ) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientModRepository.findByLogin(login).orElseThrow(ClientNotFoundException::new);
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

    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = false,
            timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasRole('CLIENT')")
    public PeriodicSurveyDTO editPeriodicSurvey(PeriodicSurveyDTO dto) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientModRepository.findByLogin(login).orElseThrow(ClientNotFoundException::new);
        PeriodicSurvey existing = periodicSurveyRepository.findTopByClientOrderByMeasurementDateDesc(client).orElseThrow(PeriodicSurveyNotFound::new);
        LockTokenService.Record<UUID, Long> record = lockTokenService.verifyToken(dto.getLockToken());
        if (!existing.getId().equals(record.id())) {
            throw new InvalidLockTokenException();
        }
        if (!existing.getVersion().equals(record.version())) {
            throw new ConcurrentUpdateException();
        }
        existing.setBloodPressure(dto.getBloodPressure());
        existing.setWeight(dto.getWeight());
        existing.setBloodSugarLevel(dto.getBloodSugarLevel());
        existing.setMeasurementDate(Timestamp.from(Instant.now()));
        return periodicSurveyMapper.toPeriodicSurveyDTO(periodicSurveyRepository.saveAndFlush(existing));
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
    @PreAuthorize("hasRole('CLIENT')|| hasRole('DIETICIAN')") //todo sprawdzic czy tu dietetyk ma zostac
    public PeriodicSurveyDTO getMyLatestPeriodicSurvey() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientModRepository.findByLogin(login).orElseThrow(ClientNotFoundException::new);
        PeriodicSurvey survey = periodicSurveyRepository.findTopByClientOrderByMeasurementDateDesc(client).orElseThrow(PeriodicSurveyNotFound::new);
        return periodicSurveyMapper.toPeriodicSurveyDTO(survey);
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = true,
            timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasRole('CLIENT')")
    public BloodTestOrderDTO getBloodTestOrder() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientModRepository.findByLogin(login).orElseThrow(ClientNotFoundException::new);
        BloodTestOrder bloodTestOrder = bloodTestOrderRepository.
                getAllByClient_IdAndFulfilled(client.getId(), false).orElseThrow(BloodTestOrderNotFoundException::new);
        return bloodTestOrderMapper.toBloodTestOrderDTO(bloodTestOrder);
    }
}
