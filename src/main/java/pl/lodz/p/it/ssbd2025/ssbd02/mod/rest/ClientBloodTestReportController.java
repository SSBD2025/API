package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientBloodTestReportDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.UpdateBloodTestReportDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.ClientBloodTestReportMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientBloodTestReport;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.AuthorizedEndpoint;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.ClientBloodTestReportService;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.ClientModService;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientBloodTestReportService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled = true)
@RequestMapping("/api/mod/blood-test-reports")
public class ClientBloodTestReportController {

    private final IClientBloodTestReportService clientBloodTestReportService;
    private final ClientBloodTestReportMapper clientBloodTestReportMapper;
    private final LockTokenService lockTokenService;
    private final ClientModService clientModService;

    @PreAuthorize("hasRole('DIETICIAN')")
    @PostMapping("/client/{clientId}")
    public ResponseEntity<Object> createClientBloodTestReport(@PathVariable SensitiveDTO clientId, @RequestBody @Validated(OnCreate.class) ClientBloodTestReportDTO report) {
        ClientBloodTestReport newClientBloodTestResult = clientBloodTestReportMapper.toNewClientBloodTestReport(report);
        ClientBloodTestReportDTO dto = clientBloodTestReportMapper.toClientBloodTestReportDTO(
                clientBloodTestReportService.createReport(clientId, newClientBloodTestResult),
                clientModService.getClientById(UUID.fromString(clientId.getValue())).getSurvey().isGender());
        dto.setLockToken(lockTokenService.generateToken(dto.getId(), dto.getVersion()).getValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PreAuthorize("hasRole('DIETICIAN')")
    @GetMapping("/client/{clientId}")
    @AuthorizedEndpoint
    @Operation(summary = "Zwraca wszystkie wyniki badań krwi dla określonego klienta",
            description = "Dostępne dla DIETICIAN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operacja powiodła się"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono wyników"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono ankiety parametrów stałych"),
    })
    public ResponseEntity<List<ClientBloodTestReportDTO>> getAllByClientId(@PathVariable UUID clientId) {
        return ResponseEntity.ok().body(clientBloodTestReportService.getAllByClientId(new SensitiveDTO(clientId.toString())));
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/client")
    @AuthorizedEndpoint
    @Operation(summary = "Zwraca wszystkie wyniki badań krwi dla klienta",
            description = "Dostępne dla CLIENT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operacja powiodła się"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono wyników"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono ankiety parametrów stałych"),
    })
    public ResponseEntity<List<ClientBloodTestReportDTO>> getAllByClientLogin() {
        return ResponseEntity.ok().body(clientBloodTestReportService.getAllByClientLogin());
    }

    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    @GetMapping("/{reportId}")
    public ResponseEntity<ClientBloodTestReportDTO> getById(@PathVariable UUID reportId) {
        return ResponseEntity.ok().body(clientBloodTestReportService.getById(new SensitiveDTO(reportId.toString())));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DIETICIAN')")
    @Operation(summary = "Edytuj wyniki badań krwi", description = "Dostępne dla DIETICIAN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operacja powiodła się"),
            @ApiResponse(responseCode = "401", description = "Token niepoprawny"),
            @ApiResponse(responseCode = "401", description = "Niepoprawna sygnatura tokenu"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono wyniku badań krwi do edycji"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono rezultatu do edycji"),
            @ApiResponse(responseCode = "409", description = "Wykryto rozbieżność wersji encji wyników badań krwi"),
    })
    public ResponseEntity<?> editBloodTestReport(@RequestBody @Validated(OnUpdate.class) ClientBloodTestReportDTO result) {
        clientBloodTestReportService.updateReport(result);
        return ResponseEntity.ok().build();
    }
}
