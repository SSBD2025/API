package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.*;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientFoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.AuthorizedEndpoint;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientFoodPyramidService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/client-food-pyramids")
@EnableMethodSecurity(prePostEnabled = true)
@MethodCallLogged
public class ClientFoodPyramidController {
    private final IClientFoodPyramidService clientFoodPyramidService;

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ClientFoodPyramidDTO>> getByClientId(@PathVariable UUID clientId) {
        // Implementation will be added later
        return null;
    }

    @PostMapping
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

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/client-pyramids")
    public ResponseEntity<List<ClientFoodPyramidDTO>> getClientPyramids() {
        List<ClientFoodPyramidDTO> pyramids = clientFoodPyramidService.getClientPyramids();
        if (pyramids.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(pyramids);
    }

    @PreAuthorize("hasRole('DIETICIAN')")
    @GetMapping("/dietician/{clientId}")
    public ResponseEntity<List<ClientFoodPyramidDTO>> getClientPyramidsAsDietician(
            @PathVariable UUID clientId) {
        List<ClientFoodPyramidDTO> pyramids = clientFoodPyramidService.getClientPyramidsByDietician(clientId);
        if (pyramids.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(pyramids);
    }

    @GetMapping("/current")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Pobierz dane aktualnie przypisanej piramidy do zalogowanego użytkownika",
            description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dane piramidy zostały zwrócone"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta o podanym loginie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono piramidy dla tego klienta")
    })
    public ResponseEntity<ClientFoodPyramidDTO> getCurrentFoodPyramid() {
        return ResponseEntity.ok(clientFoodPyramidService.getMyCurrentPyramid());
    }

    @GetMapping("/client/{clientId}/current")
    @PreAuthorize("hasRole('DIETICIAN')")
    @Operation(summary = "Pobierz dane aktualnie przypisanej piramidy do użytkownika po podanym ID",
            description = "Dostępne dla DIETICIAN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dane piramidy zostały zwrócone"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta o podanym id"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono piramidy dla tego klienta")
    })
    public ResponseEntity<ClientFoodPyramidDTO> getCurrentFoodPyramid(@PathVariable UUID clientId) {
        return ResponseEntity.ok(clientFoodPyramidService.getCurrentPyramid(clientId));
    }
}
