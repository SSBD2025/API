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
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AssignDietPlanDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientFoodPyramidDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FoodPyramidDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.FoodPyramidMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientFoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.*;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientFoodPyramidRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientModRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.DieticianModRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.FoodPyramidRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientFoodPyramidService;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    private final DieticianModRepository dieticianModRepository;
    private final FoodPyramidMapper foodPyramidMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
    public List<ClientFoodPyramid> getByClientId(UUID clientId) {
        return clientFoodPyramidRepository.findAllByClientId(clientId);
    }

    @Override
    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(
            propagation = Propagation.REQUIRED,
            transactionManager = "modTransactionManager",
            readOnly = false,
            timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    public void assignFoodPyramidToClient(AssignDietPlanDTO dto) {
        Client client = clientModRepository.findClientById(dto.getClientId()).orElseThrow(ClientNotFoundException::new);
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

    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    @Override
    public ClientFoodPyramid createAndAssignFoodPyramid(FoodPyramidDTO dto, SensitiveDTO clientId) {
        FoodPyramid pyramidToCompare = foodPyramidMapper.toEntity(dto);
        List<FoodPyramid> allPyramids = StreamSupport.stream(
                        foodPyramidRepository.findAll().spliterator(),
                        false)
                .toList();
        Optional<FoodPyramid> existingPyramid = allPyramids.stream()
                .filter(p -> p.equals(pyramidToCompare))
                .findFirst();
        FoodPyramid foodPyramid = existingPyramid.orElseGet(() -> foodPyramidService.createFoodPyramid(dto));
        assignFoodPyramidToClient(new AssignDietPlanDTO(UUID.fromString(clientId.getValue()), foodPyramid.getId()));
        return getByClientId(UUID.fromString(clientId.getValue())).stream()
                .max(Comparator.comparing(ClientFoodPyramid::getTimestamp))
                .orElseThrow(FoodPyramidNotFoundException::new);
    }

    @Override
    @PreAuthorize("hasRole('CLIENT')")
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = true,
            timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    public List<ClientFoodPyramidDTO> getClientPyramids() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientModRepository.findByLogin(login).orElseThrow(ClientNotFoundException::new);
        List<ClientFoodPyramid> clientFoodPyramids = clientFoodPyramidRepository.findByClientIdOrderByTimestampDesc(client.getId());
        if (clientFoodPyramids.isEmpty()) {
            return List.of();
        }
        UUID latestPyramidId = clientFoodPyramids.getFirst().getFoodPyramid().getId();
        List<ClientFoodPyramidDTO> result = new ArrayList<>();
        for (ClientFoodPyramid p : clientFoodPyramids) {
            FoodPyramidDTO dto = foodPyramidMapper.toDto(p.getFoodPyramid());
            boolean isLatest = p.getFoodPyramid().getId().equals(latestPyramidId);
            result.add(new ClientFoodPyramidDTO(dto, isLatest, p.getTimestamp()));
        }
        return result;
    }

    @Override
    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = true,
            timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    public List<ClientFoodPyramidDTO> getClientPyramidsByDietician(UUID clientId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Dietician dietician = dieticianModRepository.findByLogin(login)
                .orElseThrow(DieticianNotFoundException::new);
        Client client = clientModRepository.findClientById(clientId)
                .orElseThrow(ClientNotFoundException::new);
        if (!dietician.getClients().contains(client)) {
            throw new ClientNotAssignedException();
        }
        List<ClientFoodPyramid> pyramids = clientFoodPyramidRepository
                .findByClientIdOrderByTimestampDesc(client.getId());
        if (pyramids.isEmpty()) {
            return List.of();
        }
        UUID latestId = pyramids.getFirst().getFoodPyramid().getId();
        List<ClientFoodPyramidDTO> result = new ArrayList<>();
        for (ClientFoodPyramid p : pyramids) {
            FoodPyramidDTO dto = foodPyramidMapper.toDto(p.getFoodPyramid());
            boolean isLatest = dto.getId().equals(latestId);
            result.add(new ClientFoodPyramidDTO(dto, isLatest, p.getTimestamp()));
        }
        return result;
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW, readOnly = true,
            transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('CLIENT')")
    @Retryable(
            retryFor = {JpaSystemException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    public ClientFoodPyramidDTO getMyCurrentPyramid() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientModRepository.findByLogin(login).orElseThrow(ClientNotFoundException::new);

        return getLatestClientFoodPyramidDto(client);
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW, readOnly = true,
            transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('DIETICIAN')")
    @Retryable(
            retryFor = {JpaSystemException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    public ClientFoodPyramidDTO getCurrentPyramid(UUID clientId) {
        Client client = clientModRepository.findClientById(clientId).orElseThrow(ClientNotFoundException::new);

        return getLatestClientFoodPyramidDto(client);
    }

    private ClientFoodPyramidDTO getLatestClientFoodPyramidDto(Client client) {
        ClientFoodPyramid clientFoodPyramid = clientFoodPyramidRepository
                .findTopByClientOrderByTimestampDesc(client)
                .orElseThrow(FoodPyramidNotFoundException::new);

        FoodPyramidDTO foodPyramidDTO = foodPyramidMapper.toDto(clientFoodPyramid.getFoodPyramid());

        return new ClientFoodPyramidDTO(foodPyramidDTO, true, clientFoodPyramid.getTimestamp());
    }
}
