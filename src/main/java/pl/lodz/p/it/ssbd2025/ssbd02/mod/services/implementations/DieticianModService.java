package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AssignDietPlanDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.*;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ClientNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.DieticianNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.FoodPyramidNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.SurveyNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.*;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IDieticianService;

import java.sql.Timestamp;
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

}
