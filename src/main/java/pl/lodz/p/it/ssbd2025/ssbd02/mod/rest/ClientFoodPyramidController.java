package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.*;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientFoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientFoodPyramidService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/client-food-pyramids")
public class ClientFoodPyramidController {
    private final IClientFoodPyramidService clientFoodPyramidService;

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ClientFoodPyramidDTO>> getByClientId(@PathVariable UUID clientId) {
        // Implementation will be added later
        return null;
    }

    @PostMapping
    @MethodCallLogged
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('DIETICIAN')")
    public ResponseEntity<Void> assignFoodPyramidToClient(@Valid @RequestBody AssignDietPlanDTO dto) {
        clientFoodPyramidService.assignFoodPyramidToClient(dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/client/{clientId}/pyramid/{pyramidId}")
    public ResponseEntity<Void> removeFoodPyramidFromClient(@PathVariable UUID clientId, @PathVariable UUID pyramidId) {
        // Implementation will be added later
        return null;
    }

    @PostMapping("/new/{clientId}")
    @MethodCallLogged
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('DIETICIAN')")
    public ResponseEntity<ClientFoodPyramid> createAndAssignFoodPyramidToClient(@Valid @RequestBody FoodPyramidDTO dto, @PathVariable UUID clientId) {
        return ResponseEntity.ok().body(clientFoodPyramidService.createAndAssignFoodPyramid(dto, new SensitiveDTO(clientId.toString())));
    }
}
