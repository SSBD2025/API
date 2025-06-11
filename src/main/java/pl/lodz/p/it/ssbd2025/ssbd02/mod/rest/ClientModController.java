package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientService;

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

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable UUID id) {
        // Implementation will be added later
        return null;
    }

    @PostMapping("/{clientId}/dietician/{dieticianId}")
    public ResponseEntity<Void> assignDietician(@PathVariable UUID clientId, @PathVariable UUID dieticianId) {
        // Implementation will be added later
        return null;
    }

    @PostMapping("/permanent-survey")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<SurveyDTO> submitPermanentSurvey(
            @Validated(OnCreate.class)
            @RequestBody SurveyDTO surveyDTO) {
        Survey newSurvey = surveyMapper.toSurvey(surveyDTO);
        Survey savedSurvey = clientService.submitPermanentSurvey(newSurvey);
        return ResponseEntity.status(HttpStatus.CREATED).body(surveyMapper.toSurveyDTO(savedSurvey));
    }

    @GetMapping("/permanent-survey")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<SurveyDTO> getPermanentSurvey() {
        return ResponseEntity.status(HttpStatus.OK).body(surveyMapper.toSurveyDTO(clientService.getPermanentSurvey()));
    }

    @PutMapping("/permanent-survey")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<SurveyDTO> editPermanentSurvey(
            @Validated(OnUpdate.class)
            @RequestBody SurveyDTO surveyDTO) {

        Survey updatedSurvey = clientService.editPermanentSurvey(surveyDTO);
        return ResponseEntity.ok(surveyMapper.toSurveyDTO(updatedSurvey));
    }

    @GetMapping("/get-available-dieticians")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<DieticianDTO>> getAvailableDieticians(
            @RequestParam(required = false) String searchPhrase
    ) {
        List<Dietician> dieticians = clientService.getAvailableDieticians(searchPhrase);
        return ResponseEntity.status(HttpStatus.OK).body(dieticianMapper.toDieticianListDTO(dieticians));
    }

    @PostMapping("/periodic-survey")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PeriodicSurveyDTO> submitPeriodicSurvey(@Validated(OnCreate.class)
                                                                  @RequestBody PeriodicSurveyDTO periodicSurveyDTO) {

        PeriodicSurvey periodicSurvey = clientService.submitPeriodicSurvey(periodicSurveyMapper.toPeriodicSurvey(periodicSurveyDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(periodicSurveyMapper.toPeriodicSurveyDTO(periodicSurvey));
    }

    @GetMapping("/{clientId}/periodic-survey")
    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    public ResponseEntity<Page<PeriodicSurveyDTO>> getPeriodicSurveysByClientId(
            @PathVariable UUID clientId,
            Pageable pageable) {

        Page<PeriodicSurveyDTO> dtoPage = clientService.getPeriodicSurveys(clientId, pageable);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/periodic-survey/{surveyId}")
    @PreAuthorize("hasRole('CLIENT') || hasRole('DIETICIAN')")
    public ResponseEntity<PeriodicSurveyDTO> getPeriodicSurveyByClientIdAndSurveyId(
            @PathVariable UUID surveyId
    ) {
        PeriodicSurveyDTO periodicSurveyDTO = clientService.getPeriodicSurvey(surveyId);
        return ResponseEntity.ok(periodicSurveyDTO);
    }

    @GetMapping("/periodic-survey")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Object> getMyPeriodicSurveys(Pageable pageable) {
        Page<PeriodicSurveyDTO> periodicSurveyDTO = clientService.getPeriodicSurveys(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(periodicSurveyDTO);
    }

    @GetMapping("/dietician/{clientId}/periodic-survey")
    @PreAuthorize("hasRole('DIETICIAN')")
    public ResponseEntity<Object> getPeriodicSurveysByAccountId(
            @PathVariable UUID clientId,
            Pageable pageable) {
        Page<PeriodicSurveyDTO> dtoPage = clientService.getPeriodicSurveysByAccountId(clientId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(dtoPage);
    }
}
