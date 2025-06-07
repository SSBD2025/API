package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

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
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.ClientBloodTestReportService;
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

    @PreAuthorize("hasRole('DIETICIAN')")
    @PostMapping("/client/{clientId}")
    public ResponseEntity<Object> createClientBloodTestReport(@PathVariable SensitiveDTO clientId, @RequestBody @Validated(OnCreate.class) ClientBloodTestReportDTO report) {
        ClientBloodTestReport newClientBloodTestResult = clientBloodTestReportMapper.toNewClientBloodTestReport(report);
        ClientBloodTestReportDTO dto = clientBloodTestReportMapper.toClientBloodTestReportDTO(clientBloodTestReportService.createReport(clientId, newClientBloodTestResult));
        dto.setLockToken(lockTokenService.generateToken(dto.getId(), dto.getVersion()).getValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PreAuthorize("hasRole('DIETICIAN')")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ClientBloodTestReportDTO>> getAllByClientId(@PathVariable UUID clientId) {
        return ResponseEntity.ok().body(clientBloodTestReportService.getAllByClientId(new SensitiveDTO(clientId.toString())));
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/client")
    public ResponseEntity<List<ClientBloodTestReportDTO>> getAllByClientLogin() {
        return ResponseEntity.ok().body(clientBloodTestReportService.getAllByClientLogin());
    }

    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')")
    @GetMapping("/{reportId}")
    public ResponseEntity<ClientBloodTestReportDTO> getById(@PathVariable UUID reportId) {
        return ResponseEntity.ok().body(clientBloodTestReportService.getById(new SensitiveDTO(reportId.toString())));
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable UUID reportId) {
        // Implementation will be added later
        return null;
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DIETICIAN')")
    public ResponseEntity<?> editBloodTestReport(@RequestBody @Validated(OnUpdate.class) ClientBloodTestReportDTO result) {
        clientBloodTestReportService.updateReport(result);
        return ResponseEntity.ok().build();
    }
}
