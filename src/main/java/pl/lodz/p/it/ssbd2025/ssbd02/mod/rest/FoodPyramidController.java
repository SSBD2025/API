package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FoodPyramidDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.FoodPyramidService;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IFoodPyramidService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/food-pyramids")
public class FoodPyramidController {

    private final FoodPyramidService foodPyramidService;

    @GetMapping("/{id}")
    public ResponseEntity<FoodPyramidDTO> getById(@PathVariable UUID id) {
        // Implementation will be added later
        return null;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('DIETICIAN')")
    public List<FoodPyramidDTO> getAllFoodPyramids() {
        return foodPyramidService.getAllFoodPyramids();
    }
}
