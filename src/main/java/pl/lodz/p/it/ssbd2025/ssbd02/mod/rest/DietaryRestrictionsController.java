package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.DietaryRestrictionsDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.DietaryRestrictionsService;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IDietaryRestrictionsService;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/dietary-restrictions")
public class DietaryRestrictionsController {

    @GetMapping("/client/{clientId}")
    public ResponseEntity<DietaryRestrictionsDTO> getByClientId(@PathVariable UUID clientId) {
        // Implementation will be added later
        return null;
    }

    @PutMapping("/client/{clientId}")
    public ResponseEntity<DietaryRestrictionsDTO> updateRestrictions(@PathVariable UUID clientId, @RequestBody DietaryRestrictionsDTO restrictions) {
        // Implementation will be added later
        return null;
    }
}
