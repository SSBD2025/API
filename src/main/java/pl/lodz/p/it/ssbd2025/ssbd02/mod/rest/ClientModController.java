package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SurveyDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.ClientMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.SurveyMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
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
    private final ClientMapper clientMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable UUID id) {
        // Implementation will be added later
        return null;
    }

    @GetMapping("/dietician")
    @PreAuthorize("hasRole('DIETICIAN')")
    public ResponseEntity<List<ClientDTO>> getClientsByDietician() {
        List<Client> clients = clientService.getClientsByDietician();
        return ResponseEntity.status(HttpStatus.OK).body(clientMapper.toClientListDTO(clients));
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
}
