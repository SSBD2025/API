package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FoodPyramidDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.FoodPyramidMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.AlgorithmService;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IAlgorithmService;

import java.util.UUID;

@RestController
@RequestMapping("/api/mod/algorithm")
@EnableMethodSecurity(prePostEnabled=true)
@RequiredArgsConstructor
public class AlgorithmController {

    private final IAlgorithmService algorithmService;
    private final FoodPyramidMapper foodPyramidMapper;

    @PreAuthorize("hasRole('DIETICIAN')")
    @GetMapping("/{clientId}")
    public ResponseEntity<FoodPyramidDTO> algorithm(@PathVariable UUID clientId) {
        return ResponseEntity.ok().body(foodPyramidMapper.toDto(algorithmService.generateFoodPyramid(clientId)));
    }
}
