package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodTestResultDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.BloodTestResultService;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IBloodTestResultService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/blood-test-results")
public class BloodTestResultController {

    @GetMapping("/report/{reportId}")
    public ResponseEntity<List<BloodTestResultDTO>> getAllByReportId(@PathVariable UUID reportId) {
        // Implementation will be added later
        return null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BloodTestResultDTO> getById(@PathVariable UUID id) {
        // Implementation will be added later
        return null;
    }

    @PostMapping("/report/{reportId}")
    public ResponseEntity<BloodTestResultDTO> addBloodTestResult(@PathVariable UUID reportId, @RequestBody BloodTestResultDTO result) {
        // Implementation will be added later
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBloodTestResult(@PathVariable UUID id) {
        // Implementation will be added later
        return null;
    }
}
