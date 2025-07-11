package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.ClientMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.DieticianMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.PeriodicSurveyMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.SurveyMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.PeriodicSurvey;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.AuthorizedEndpoint;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/clients")
@EnableMethodSecurity(prePostEnabled = true)
@MethodCallLogged
public class ClientModController {
    private final IClientService clientService;
    private final SurveyMapper surveyMapper;
    private final DieticianMapper dieticianMapper;
    private final PeriodicSurveyMapper periodicSurveyMapper;
    private final LockTokenService lockTokenService;

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/assign-dietician/{dieticianId}")
    @Operation(summary = "Przypisz dietetyka do klienta",
            description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dietetyk został przypisany"),
            @ApiResponse(responseCode = "400", description = "Niepoprawne dane wejściowe"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono dietetyka o podanym Id; nie znaleziono klienta"),
            @ApiResponse(responseCode = "409", description = "Inny dietetyk jest już przypisany do klienta; ten dietetyk jest już przypisany do klienta; dietetyk osiągnął już limit klientów")
    })
    public ResponseEntity<Void> assignDietician(@PathVariable UUID dieticianId) {
        clientService.assignDietician(dieticianId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/status")
    @Operation(summary = "Probierz status klienta",
            description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zwrócono status klienta")
    })
    public ResponseEntity<ClientStatusDTO> getClientStatus() {
        return ResponseEntity.ok(clientService.getClientStatus());
    }

    @PostMapping("/permanent-survey")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Wypełnij ankietę parametrów stałych",
            description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Wypełniono ankietę parametrów stałych"),
            @ApiResponse(responseCode = "400", description = "Niepoprawne dane wejściowe"),
            @ApiResponse(responseCode = "409", description = "Klient już wypełnił ankietę; klient nie ma przypisangeo dietetyka"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe"),
    })
    public ResponseEntity<SurveyDTO> submitPermanentSurvey(
            @Validated(OnCreate.class)
            @RequestBody SurveyDTO surveyDTO) {
        Survey newSurvey = surveyMapper.toSurvey(surveyDTO);
        Survey savedSurvey = clientService.submitPermanentSurvey(newSurvey);
        return ResponseEntity.status(HttpStatus.CREATED).body(surveyMapper.toSurveyDTO(savedSurvey));
    }

    @GetMapping("/permanent-survey")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Pobierz swoją ankietę parametrów stałych",
            description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ankieta parametrów stałych została pomyślnie pobrana"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta o podanym loginie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono ankiety parametrów stałych dla klienta"),
    })
    public ResponseEntity<SurveyDTO> getPermanentSurvey() {
        Survey survey = clientService.getPermanentSurvey();
        SurveyDTO responseDTO = surveyMapper.toSurveyDTO(survey);
        responseDTO.setLockToken(lockTokenService.generateToken(survey.getId(), survey.getVersion()).getValue());
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    @PutMapping("/permanent-survey")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Edytuj ankietę stałą", description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ankieta stała została zaktualizowana"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do edycji ankiety"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono ankiety"),
            @ApiResponse(responseCode = "409", description = "Konflikt wersji danych")
    })
    public ResponseEntity<SurveyDTO> editPermanentSurvey(
            @Validated(OnUpdate.class)
            @RequestBody SurveyDTO surveyDTO) {

        Survey updatedSurvey = clientService.editPermanentSurvey(surveyDTO);
        return ResponseEntity.ok(surveyMapper.toSurveyDTO(updatedSurvey));
    }

    @GetMapping("/get-available-dieticians")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Pobierz dostępnych dietetyków",
            description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zwrócono listę dostępnych dietetyków")
    })
    public ResponseEntity<List<DieticianDTO>> getAvailableDieticians(
            @RequestParam(required = false) String searchPhrase
    ) {
        List<Dietician> dieticians = clientService.getAvailableDieticians(searchPhrase);
        return ResponseEntity.status(HttpStatus.OK).body(dieticianMapper.toDieticianListDTO(dieticians));
    }

    @PostMapping("/periodic-survey")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Wypełnij ankietę okresową",
            description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ankieta okresowa została pomyślnie dodana"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta o podanym loginie"),
            @ApiResponse(responseCode = "409", description = "Nie mineły 24 godziny od ostatniego wypełnienia ankiety okresowej")
    })
    public ResponseEntity<PeriodicSurveyDTO> submitPeriodicSurvey(@Validated(OnCreate.class)
                                                                  @RequestBody PeriodicSurveyDTO periodicSurveyDTO) {

        PeriodicSurvey periodicSurvey = clientService.submitPeriodicSurvey(periodicSurveyMapper.toPeriodicSurvey(periodicSurveyDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(periodicSurveyMapper.toPeriodicSurveyDTO(periodicSurvey));
    }

    @GetMapping("/{clientId}/periodic-survey")
    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Operation(summary = "Pobierz liste ankiet okresowych dla podanego klienta",
            description = "Dostępne dla CLIENT, DIETICIAN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista ankiet okresowych została zwrócona"),
            @ApiResponse(responseCode = "400", description = "Niepoprawne żądanie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta o podanym id"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono ankiet okresowych")
    })
    public ResponseEntity<PagedModel<EntityModel<PeriodicSurveyDTO>>> getPeriodicSurveysByClientId(
            @PathVariable UUID clientId,
            Pageable pageable,
            PagedResourcesAssembler<PeriodicSurveyDTO> pagedResourcesAssembler) {

        Page<PeriodicSurveyDTO> dtoPage = clientService.getPeriodicSurveys(clientId, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(dtoPage));
    }

    @GetMapping("/periodic-survey/{surveyId}")
    @PreAuthorize("hasRole('CLIENT') || hasRole('DIETICIAN')")
    @Operation(summary = "Pobierz ankietę okresowyą dla podanego ID",
            description = "Dostępne dla CLIENT, DIETICIAN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ankieta okresowa została zwrócona"),
            @ApiResponse(responseCode = "400", description = "Niepoprawne żądanie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono ankiety okresowej")
    })
    public ResponseEntity<PeriodicSurveyDTO> getPeriodicSurveyByClientIdAndSurveyId(
            @PathVariable UUID surveyId
    ) {
        PeriodicSurveyDTO periodicSurveyDTO = clientService.getPeriodicSurvey(surveyId);
        return ResponseEntity.ok(periodicSurveyDTO);
    }

    @GetMapping("/periodic-survey")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Pobierz listę swoich ankiet okresowych",
            description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista ankiet została zwrócona"),
            @ApiResponse(responseCode = "404", description = "Nie odnaleziono ankiet okresowych"),
            @ApiResponse(responseCode = "404", description = "Nie odnaleziono klienta")
    })
    public ResponseEntity<PagedModel<EntityModel<PeriodicSurveyDTO>>> getMyPeriodicSurveys(
            Pageable pageable,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime since,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime before,
            PagedResourcesAssembler<PeriodicSurveyDTO> pagedResourcesAssembler
    ) {
        Page<PeriodicSurveyDTO> periodicSurveyDTO = clientService.getPeriodicSurveys(pageable,
                since != null ? Timestamp.valueOf(since) : null,
                before != null ? Timestamp.valueOf(before) : null);
        return ResponseEntity.status(HttpStatus.OK).body(pagedResourcesAssembler.toModel(periodicSurveyDTO));
    }

    @PutMapping("/periodic-survey")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Edytuj swoją ostatnią ankietę okresową",
            description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ankieta została zmodyfikowana"),
            @ApiResponse(responseCode = "400", description = "Niepoprawnie sformatowane dane"),
            @ApiResponse(responseCode = "403", description = "Niepoprawny lock token"),
            @ApiResponse(responseCode = "404", description = "Nie odnaleziono klienta"),
            @ApiResponse(responseCode = "404", description = "Nie odnaleziono ankiety okresowej"),
            @ApiResponse(responseCode = "409", description = "Konflikt wersji danych")
    })
    public ResponseEntity<Object> editPeriodicSurvey(
            @Validated(OnUpdate.class) @RequestBody PeriodicSurveyDTO periodicSurveyDTO) {
        PeriodicSurveyDTO dto = clientService.editPeriodicSurvey(periodicSurveyDTO);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @GetMapping("/periodic-survey/latest")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Pobranie ostatniej ankiety okresowej",
            description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ankieta została zmodyfikowana"),
            @ApiResponse(responseCode = "404", description = "Nie odnaleziono klienta"),
            @ApiResponse(responseCode = "404", description = "Nie odnaleziono ankiety okresowej"),
    })
    public ResponseEntity<Object> getMyLatestPeriodicSurvey() {
        PeriodicSurveyDTO periodicSurveyDTO = clientService.getMyLatestPeriodicSurvey();
        return ResponseEntity.status(HttpStatus.OK).body(periodicSurveyDTO);
    }

    @GetMapping("/blood-test-order")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Pobranie zlecenia na badanie krwi",
            description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Badanie krwi zostało zwrócone"),
            @ApiResponse(responseCode = "404", description = "Nie odnaleziono klienta"),
            @ApiResponse(responseCode = "404", description = "Nie odnaleziono zlecenia na badanie krwi"),
    })
    public ResponseEntity<Object> getBloodTestOrders() {
        BloodTestOrderDTO dto = clientService.getBloodTestOrder();
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }
}