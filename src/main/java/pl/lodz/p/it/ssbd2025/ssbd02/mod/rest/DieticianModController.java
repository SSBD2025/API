package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.BloodTestOrderMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.ClientMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.SurveyMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestOrder;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.DieticianModService;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IDieticianService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/dieticians")
public class DieticianModController {

    private final IDieticianService dieticianModService;

    private final ClientMapper clientMapper;
    private final SurveyMapper surveyMapper;
    private final BloodTestOrderMapper bloodTestOrderMapper;

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

    @GetMapping("/{clientId}/details")
    @PreAuthorize("hasRole('DIETICIAN')")
    public ResponseEntity<ClientDetailsDTO> getClientDetails(@PathVariable UUID clientId) {
        Client client = dieticianModService.getClientDetails(clientId);
        return ResponseEntity.status(HttpStatus.OK).body(clientMapper.toDetailsDto(client));
    }

    @PreAuthorize("hasRole('DIETICIAN')")
    @PostMapping("/order-medical-examinations")
    public ResponseEntity<BloodTestOrderDTO> orderMedicalExaminations(
            @Validated(OnCreate.class)
            @RequestBody BloodTestOrderDTO bloodTestOrderDTO
    ) {
        BloodTestOrder bloodTestOrder = dieticianModService.orderMedicalExaminations(bloodTestOrderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(bloodTestOrderMapper.toBloodTestOrderDTO(bloodTestOrder));
    }

    @PreAuthorize("hasRole('DIETICIAN')")
    @GetMapping("/orders")
    public ResponseEntity<List<BloodTestOrderDTO>> getOrders() {
        List<BloodTestOrder> bloodTestOrders = dieticianModService.getUnfulfilledBloodTestOrders();
        return ResponseEntity.status(HttpStatus.OK).body(bloodTestOrderMapper.toBloodTestOrderDTOs(bloodTestOrders));
    }

    @PreAuthorize("hasRole('DIETICIAN')")
    @PatchMapping("/{orderId}/confirm")
    public ResponseEntity<Void> confirmBloodTestOrder(@PathVariable UUID orderId) {
        dieticianModService.confirmBloodTestOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clientId}/periodic-surveys")
    @PreAuthorize("hasRole('DIETICIAN')")
    public ResponseEntity<Object> getPeriodicSurveysByAccountId(
            @PathVariable UUID clientId,
            Pageable pageable,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime since,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime before
            ) {

        Page<PeriodicSurveyDTO> dtoPage = dieticianModService.getPeriodicSurveysByAccountId(clientId, pageable,
                since != null ? Timestamp.valueOf(since) : null,
                before != null ? Timestamp.valueOf(before) : null);
        return ResponseEntity.status(HttpStatus.OK).body(dtoPage);
    }
}
