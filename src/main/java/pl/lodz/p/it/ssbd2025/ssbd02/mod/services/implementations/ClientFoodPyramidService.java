package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

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
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AssignDietPlanDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FoodPyramidDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientFoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ClientNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.FoodPyramidAlreadyAssignedException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.FoodPyramidNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientFoodPyramidRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientModRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.FoodPyramidRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientFoodPyramidService;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@TransactionLogged
@Component
@RequiredArgsConstructor
@Service
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
public class ClientFoodPyramidService implements IClientFoodPyramidService {
    private final ClientModRepository clientModRepository;
    private final FoodPyramidRepository foodPyramidRepository;
    private final ClientFoodPyramidRepository clientFoodPyramidRepository;
    private final FoodPyramidService foodPyramidService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
    public List<ClientFoodPyramid> getByClientId(UUID clientId) {
        return clientFoodPyramidRepository.findAllByClientId(clientId);
    }

    @Override
    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = false,
            timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    public void assignFoodPyramidToClient(AssignDietPlanDTO dto) {
        Client client = clientModRepository.findById(dto.getClientId()).orElseThrow(ClientNotFoundException::new);
        FoodPyramid foodPyramid = foodPyramidRepository.findById(dto.getFoodPyramidId()).orElseThrow(FoodPyramidNotFoundException::new);
        boolean alreadyAssigned = clientFoodPyramidRepository
                .existsByClientAndFoodPyramid(client, foodPyramid);
        if (alreadyAssigned) {
            throw new FoodPyramidAlreadyAssignedException();
        }
        Timestamp now = Timestamp.valueOf(java.time.LocalDateTime.now());
        ClientFoodPyramid assignment = new ClientFoodPyramid(client, foodPyramid, now);
        clientFoodPyramidRepository.saveAndFlush(assignment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
    @Override
    public ClientFoodPyramid createAndAssignFoodPyramid(FoodPyramidDTO dto, SensitiveDTO clientId) {
        FoodPyramid foodPyramid = foodPyramidService.createFoodPyramid(dto);
        assignFoodPyramidToClient(new AssignDietPlanDTO(UUID.fromString(clientId.getValue()), foodPyramid.getId()));
        return getByClientId(UUID.fromString(clientId.getValue())).stream()
                .max(Comparator.comparing(ClientFoodPyramid::getTimestamp))
                .orElseThrow(FoodPyramidNotFoundException::new);
    }

    @Override
    public void removeFoodPyramidFromClient(UUID clientId, UUID pyramidId) {

    }
}
