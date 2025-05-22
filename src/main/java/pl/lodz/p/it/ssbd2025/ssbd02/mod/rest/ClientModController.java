package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.ClientService;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IClientService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/clients")
public class ClientModController {

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable UUID id) {
        // Implementation will be added later
        return null;
    }

    @GetMapping("/dietician/{dieticianId}")
    public ResponseEntity<List<ClientDTO>> getClientsByDieticianId(@PathVariable UUID dieticianId) {
        // Implementation will be added later
        return null;
    }

    @PostMapping("/{clientId}/dietician/{dieticianId}")
    public ResponseEntity<Void> assignDietician(@PathVariable UUID clientId, @PathVariable UUID dieticianId) {
        // Implementation will be added later
        return null;
    }
}
