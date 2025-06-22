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

    @PostMapping
    @PreAuthorize("hasRole('DIETICIAN')")
    @Operation(summary = "Przypisz piramidę żywieniową do klienta",
            description = "Dostępne dla DIETICIAN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Piramida żywieniowa została pomyślnie przypisana klientowi"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta o podanym ID lub piramidy o podanym ID"),
            @ApiResponse(responseCode = "409", description = "Piramida żywieniowa jest już przypisana temu klientowi"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłwe dane wejściowe")
    })
    public ResponseEntity<Void> assignFoodPyramidToClient(@Valid @RequestBody AssignDietPlanDTO dto) {
        clientFoodPyramidService.assignFoodPyramidToClient(dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/new/{clientId}")
    @MethodCallLogged
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('DIETICIAN')")
    @Operation(summary = "Stwórz i przypisz piramidę żywieniową do klienta",
    description = "Dostępne dla DIETICIAN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Piramida została stworzona i przypisana do klienta"),
            @ApiResponse(responseCode = "200", description = "Piramida o identycznych parametrach została przypisana do klienta"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta o podanym ID"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono piramidy o podanym ID"),
            @ApiResponse(responseCode = "409", description = "Piramida żywieniowa jest już przypisana temu klientowi")
    })
    public ResponseEntity<ClientFoodPyramid> createAndAssignFoodPyramidToClient(@Valid @RequestBody FoodPyramidDTO dto, @PathVariable UUID clientId) {
        return ResponseEntity.ok().body(clientFoodPyramidService.createAndAssignFoodPyramid(dto, new SensitiveDTO(clientId.toString())));
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/client-pyramids")
    @Operation(summary = "Pobierz piramidy żywieniowe klienta",
            description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zwrócono listę piramid żywieniowych"),
            @ApiResponse(responseCode = "204", description = "Zwrócono pustą listę piramid żywieniowych"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta"),
    })
    public ResponseEntity<List<ClientFoodPyramidDTO>> getClientPyramids() {
        List<ClientFoodPyramidDTO> pyramids = clientFoodPyramidService.getClientPyramids();
        if (pyramids.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(pyramids);
    }

    @PreAuthorize("hasRole('DIETICIAN')")
    @GetMapping("/dietician/{clientId}")
    @Operation(summary = "Pobierz piramidy żywieniowe klienta przypisanego do dietetyka",
            description = "Dostępne dla DIETICIAN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zwrócono listę piramid żywieniowych"),
            @ApiResponse(responseCode = "204", description = "Zwrócono pustą listę piramid żywieniowych"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono dietetyka; nie znaleziono klienta"),
    })
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
