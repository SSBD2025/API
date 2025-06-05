package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AssignDietPlanDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientFoodPyramidDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.ClientFoodPyramidService;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientFoodPyramidService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/client-food-pyramids")
public class ClientFoodPyramidController {
    private final ClientFoodPyramidService clientFoodPyramidService;

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ClientFoodPyramidDTO>> getByClientId(@PathVariable UUID clientId) {
        // Implementation will be added later
        return null;
    }

    @PostMapping
    public ResponseEntity<Void> assignFoodPyramidToClient(@Valid @RequestBody AssignDietPlanDTO dto) {
        clientFoodPyramidService.assignFoodPyramidToClient(dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/client/{clientId}/pyramid/{pyramidId}")
    public ResponseEntity<Void> removeFoodPyramidFromClient(@PathVariable UUID clientId, @PathVariable UUID pyramidId) {
        // Implementation will be added later
        return null;
    }
}
