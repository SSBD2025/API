package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FoodPyramidDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FoodPyramidDetailsDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.FoodPyramidMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.AuthorizedEndpoint;
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

    @PreAuthorize("hasRole('DIETICIAN')")
    @GetMapping("/{id}")
    @Operation(summary = "Pobierz dane szczegółowe piramidy po id",
            description = "Dostępne dla DIETICIAN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dane piramidy zostały zwrócone"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono piramidy o podanym id")
    })
    public ResponseEntity<FoodPyramidDetailsDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(foodPyramidService.getById(id));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('DIETICIAN')")
    @Operation(summary = "Pobierz listę wszystkich piramid żywieniowych",
            description = "Dostępne dla DIETICIAN.")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pobrano listę piramid żywieniowych (może być pusta)")
    })
    public List<FoodPyramidDTO> getAllFoodPyramids() {
        return foodPyramidService.getAllFoodPyramids();
    }
}
