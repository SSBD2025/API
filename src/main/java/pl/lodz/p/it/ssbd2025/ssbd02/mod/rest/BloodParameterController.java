package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodParameterDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.BloodParameter;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ClientNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.SurveyNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.AuthorizedEndpoint;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientModRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.ClientModService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@EnableMethodSecurity(prePostEnabled = true)
@RequestMapping("/api/mod/blood-parameters")
public class BloodParameterController {

    private final ClientModService clientModService;

    @PreAuthorize("hasRole('DIETICIAN')")
    @GetMapping("/{clientId}")
    @AuthorizedEndpoint
    @Operation(summary = "Pobranie listy obsługiwanych parametrów krwi",
            description = "Dostępne dla DIETICIAN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista pobrana poprawnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono ankiety parametrów stałych"),
    })
    public List<BloodParameterDTO> getAllBloodParameters(@PathVariable SensitiveDTO clientId) {
        Survey survey = clientModService.getClientById(UUID.fromString(clientId.getValue())).getSurvey();
        if (survey == null) {
            throw new SurveyNotFoundException();
        }
        boolean male = survey.isGender();
        return Arrays.stream(BloodParameter.values())
                .map(param -> new BloodParameterDTO(
                        param.name(),
                        param.getDescription(),
                        param.getUnit().toString(),
                        male ? param.getMenStandardMin() : param.getWomanStandardMin(),
                        male ? param.getMenStandardMax() : param.getWomanStandardMax()
                ))
                .collect(Collectors.toList());
    }
}
