package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.AccessRole;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.AuthorizedEndpoint;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IAccountService;

import java.util.UUID;

@RestController
@RequestMapping("/api/account/{accountId}/roles")
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@RequiredArgsConstructor
public class UserRoleController {
    @NotNull
    private final IAccountService accountService;

    @PutMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Przypisz rolę ADMIN użytkownikowi",
            description = "Dostępne dla użytkowników z rolą ADMIN.")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rola została przypisana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta"),
            @ApiResponse(responseCode = "409", description = "Konflikt ról lub próba przypisania roli samemu sobie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowy format identyfikatora konta (UUID)")
    })
    public ResponseEntity<Void> assignAdminRole(@NotNull @PathVariable UUID accountId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.assignRole(accountId, AccessRole.ADMIN, login);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/dietician")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Przypisz rolę DIETICIAN użytkownikowi",
            description = "Dostępne dla użytkowników z rolą ADMIN. Nie można przypisać, jeśli użytkownik ma aktywną rolę CLIENT.")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rola została przypisana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta"),
            @ApiResponse(responseCode = "409", description = "Konflikt ról lub próba przypisania roli samemu sobie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowy format identyfikatora konta (UUID)")
    })

    public ResponseEntity<Void> assignDieticianRole(@NotNull @PathVariable UUID accountId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.assignRole(accountId, AccessRole.DIETICIAN, login);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/client")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Przypisz rolę CLIENT użytkownikowi",
            description = "Dostępne dla użytkowników z rolą ADMIN. Nie można przypisać, jeśli użytkownik ma aktywną rolę DIETICIAN.")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rola została przypisana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta"),
            @ApiResponse(responseCode = "409", description = "Konflikt ról lub próba przypisania roli samemu sobie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowy format identyfikatora konta (UUID)")
    })
    public ResponseEntity<Void> assignClientRole(@NotNull @PathVariable UUID accountId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.assignRole(accountId, AccessRole.CLIENT, login);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Usuń rolę ADMIN użytkownikowi",
            description = "Dostępne dla użytkowników z rolą ADMIN.")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rola została usunięta pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta lub aktywnej roli"),
            @ApiResponse(responseCode = "409", description = "Próba usunięcia roli samemu sobie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowy format identyfikatora konta (UUID)")
    })
    public ResponseEntity<Void> unassignAdminRole(@NotNull @PathVariable UUID accountId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.unassignRole(accountId, AccessRole.ADMIN, login);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/dietician")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Usuń rolę DIETICIAN użytkownikowi",
            description = "Dostępne dla użytkowników z rolą ADMIN.")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rola została usunięta pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta lub aktywnej roli"),
            @ApiResponse(responseCode = "409", description = "Próba usunięcia roli samemu sobie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowy format identyfikatora konta (UUID)")
    })
    public ResponseEntity<Void> unassignDieticianRole(@NotNull @PathVariable UUID accountId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.unassignRole(accountId, AccessRole.DIETICIAN, login);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/client")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Usuń rolę CLIENT użytkownikowi",
            description = "Dostępne dla użytkowników z rolą ADMIN.")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rola została usunięta pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta lub aktywnej roli"),
            @ApiResponse(responseCode = "409", description = "Próba usunięcia roli samemu sobie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowy format identyfikatora konta (UUID)")
    })
    public ResponseEntity<Void> unassignClientRole(@NotNull @PathVariable UUID accountId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.unassignRole(accountId, AccessRole.CLIENT, login);
        return ResponseEntity.noContent().build();
    }
}
