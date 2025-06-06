package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import lombok.RequiredArgsConstructor;
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
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ClientNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.NoAssignedDieticianException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.PermanentSurveyAlreadyExistsException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.*;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientModRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.DieticianModRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.SurveyRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientService;

import java.util.List;
import java.util.UUID;

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

    @Override
    public Client getClientById(UUID id) {
        return null;
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
    public void assignDietician(UUID clientId, UUID dieticianId) {

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
            propagation = Propagation.REQUIRES_NEW, readOnly = true,
            transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('CLIENT')")
    public List<Dietician> getAvailableDieticians(String searchPhrase) {
        if (searchPhrase != null && !searchPhrase.isEmpty()) {
            return dieticianModRepository.getAllAvailableDieticiansBySearchPhrase(searchPhrase);
        }
        return dieticianModRepository.getAllAvailableDietians();
    }
}
