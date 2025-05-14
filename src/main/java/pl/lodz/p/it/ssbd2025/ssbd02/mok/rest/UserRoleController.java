package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.AccessRole;
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
    public ResponseEntity<Void> assignAdminRole(@NotNull @PathVariable UUID accountId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.assignRole(accountId, AccessRole.ADMIN, login);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/dietician")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignDieticianRole(@NotNull @PathVariable UUID accountId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.assignRole(accountId, AccessRole.DIETICIAN, login);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/client")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignClientRole(@NotNull @PathVariable UUID accountId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.assignRole(accountId, AccessRole.CLIENT, login);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unassignAdminRole(@NotNull @PathVariable UUID accountId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.unassignRole(accountId, AccessRole.ADMIN, login);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/dietician")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unassignDieticianRole(@NotNull @PathVariable UUID accountId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.unassignRole(accountId, AccessRole.DIETICIAN, login);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/client")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unassignClientRole(@NotNull @PathVariable UUID accountId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.unassignRole(accountId, AccessRole.CLIENT, login);
        return ResponseEntity.noContent().build();
    }
}
