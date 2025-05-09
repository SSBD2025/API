package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.AccountService;

import java.util.UUID;

@RestController
@RequestMapping("/api/account/{accountId}/roles")
@RequiredArgsConstructor
public class UserRoleController {

    private final AccountService accountService;

    @PutMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignAdminRole(@PathVariable UUID accountId) {
        boolean assigned = accountService.assignAdminRole(accountId);
        if (assigned) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/dietician")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignDieticianRole(@PathVariable UUID accountId) {
        boolean assigned = accountService.assignDieticianRole(accountId);
        if (assigned) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/client")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignClientRole(@PathVariable UUID accountId) {
        boolean assigned = accountService.assignClientRole(accountId);
        if (assigned) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revokeAdminRole(@PathVariable UUID accountId) {
        boolean revoked = accountService.revokeAdminRole(accountId);
        if (revoked) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/dietician")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revokeDieticianRole(@PathVariable UUID accountId) {
        boolean revoked = accountService.revokeDieticianRole(accountId);
        if (revoked) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/client")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revokeClientRole(@PathVariable UUID accountId) {
        boolean revoked = accountService.revokeClientRole(accountId);
        if (revoked) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
