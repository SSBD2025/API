package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FoodPyramidDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FoodPyramidDetailsDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.ClientMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.FeedbackMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.FoodPyramidMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientFoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.FoodPyramidNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.FoodPyramidRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IFoodPyramidService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class FoodPyramidService implements IFoodPyramidService {
    private final FoodPyramidRepository foodPyramidRepository;
    private final FoodPyramidMapper foodPyramidMapper;
    private final FeedbackMapper feedbackMapper;
    private final ClientMapper clientMapper;

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW, readOnly = true,
            transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('DIETICIAN')")
    @Retryable(
            retryFor = {JpaSystemException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    public FoodPyramidDetailsDTO getById(UUID id) {
        FoodPyramid foodPyramid = foodPyramidRepository.findById(id).orElseThrow(FoodPyramidNotFoundException::new);

        FoodPyramidDetailsDTO foodPyramidDetailsDTO = new FoodPyramidDetailsDTO();
        foodPyramidDetailsDTO.setFoodPyramid(foodPyramidMapper.toDto(foodPyramid));
        foodPyramidDetailsDTO.setFeedbacks(feedbackMapper.toFeedbackDTOs(foodPyramid.getFeedbacks()));
        foodPyramidDetailsDTO.setClients(clientMapper.toMinimalClientListDTO(
                foodPyramid.getClientFoodPyramids().stream()
                        .map(ClientFoodPyramid::getClient)
                        .toList()
        ));

        return foodPyramidDetailsDTO;
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW, readOnly = true,
            transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('DIETICIAN')")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    public List<FoodPyramidDTO> getAllFoodPyramids() {
        return StreamSupport.stream(foodPyramidRepository.findAll().spliterator(), false)
                .map(foodPyramidMapper::toDto)
                .collect(Collectors.toList());
    }

    public FoodPyramid createFoodPyramid(FoodPyramidDTO foodPyramidDTO) {
        FoodPyramid foodPyramid = foodPyramidMapper.toEntity(foodPyramidDTO);
        return foodPyramidRepository.saveAndFlush(foodPyramid);
    }

    @Override
    public void updateAverageRating(UUID pyramidId) {

    }
}