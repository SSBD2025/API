package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AssignDietPlanDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.DieticianDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SurveyDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.ClientMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.SurveyMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.DieticianModService;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IDieticianService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/dieticians")
public class DieticianModController {

    private final IDieticianService dieticianModService;

    private final ClientMapper clientMapper;
    private final SurveyMapper surveyMapper;

    @GetMapping("/get-clients-by-dietician")
    @PreAuthorize("hasRole('DIETICIAN')")
    public ResponseEntity<List<ClientDTO>> getClientsByDietician(
            @RequestParam(required = false) String searchPhrase
    ) {
        List<Client> clients = dieticianModService.getClientsByDietician(searchPhrase);
        return ResponseEntity.status(HttpStatus.OK).body(clientMapper.toClientListDTO(clients));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DieticianDTO> getById(@PathVariable UUID id) {
        // Implementation will be added later
        return null;
    }

    @GetMapping("/{dieticianId}/clients")
    public ResponseEntity<List<ClientDTO>> getClients(@PathVariable UUID dieticianId) {
        // Implementation will be added later
        return null;
    }

    @GetMapping("/{clientId}/permanent-survey")
    @PreAuthorize("hasRole('DIETICIAN')")
    public ResponseEntity<SurveyDTO> getSurveyByClientId(@PathVariable UUID clientId) {
        Survey survey = dieticianModService.getPermanentSurveyByClientId(clientId);
        return ResponseEntity.status(HttpStatus.OK).body(surveyMapper.toSurveyDTO(survey));
    }
}
