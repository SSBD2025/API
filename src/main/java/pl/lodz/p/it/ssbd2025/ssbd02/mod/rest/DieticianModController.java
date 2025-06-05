package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AssignDietPlanDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.DieticianDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.DieticianModService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/dieticians")
public class DieticianModController {

    private final DieticianModService dieticianModService;

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
}
