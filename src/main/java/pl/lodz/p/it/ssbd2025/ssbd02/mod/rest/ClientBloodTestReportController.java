package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientBloodTestReportDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.ClientBloodTestReportService;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientBloodTestReportService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/blood-test-reports")
public class ClientBloodTestReportController {

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ClientBloodTestReportDTO>> getAllByClientId(@PathVariable UUID clientId) {
        // Implementation will be added later
        return null;
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ClientBloodTestReportDTO> getById(@PathVariable UUID reportId) {
        // Implementation will be added later
        return null;
    }

    @PostMapping("/client/{clientId}")
    public ResponseEntity<ClientBloodTestReportDTO> createReport(@PathVariable UUID clientId, @RequestBody ClientBloodTestReportDTO report) {
        // Implementation will be added later
        return null;
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable UUID reportId) {
        // Implementation will be added later
        return null;
    }
}
