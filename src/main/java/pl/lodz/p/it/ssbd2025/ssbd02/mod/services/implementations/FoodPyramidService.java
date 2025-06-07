package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FoodPyramidDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.FoodPyramidMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
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

    @Override
    public FoodPyramid getById(UUID id) {
        return null;
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW, readOnly = true,
            transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
    @PreAuthorize("hasRole('DIETICIAN')")
    @MethodCallLogged
    public List<FoodPyramidDTO> getAllFoodPyramids() {
        return StreamSupport.stream(foodPyramidRepository.findAll().spliterator(), false)
                .map(foodPyramidMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateAverageRating(UUID pyramidId) {

    }
}
