package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.SseEmitterManager;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@MethodCallLogged
@RequestMapping( "/api/sse")
public class NotificationController {
    private final SseEmitterManager sseEmitterManager;

    @GetMapping("/subscribe/block")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Subskrybuj powiadomienia o zablokowaniu konta",
            description = "Nawiązuje połączenie SSE w celu otrzymywania powiadomień w czasie rzeczywistym o zablokowaniu konta użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Połączenie SSE zostało nawiązane pomyślnie"),
            @ApiResponse(responseCode = "400",
                    description = "Podano nieprawidłowy identyfikator użytkownika")
    })
    public SseEmitter subscribeBlock(@RequestParam UUID userId) {
        return sseEmitterManager.addEmitter(userId);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/subscribe/unblock")
    @Operation(summary = "Subskrybuj powiadomienia o odblokowaniu konta",
            description = "Nawiązuje połączenie SSE w celu otrzymywania powiadomień w czasie rzeczywistym o odblokowaniu konta użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Połączenie SSE zostało nawiązane pomyślnie"),
            @ApiResponse(responseCode = "400",
                    description = "Podano nieprawidłowy identyfikator użytkownika")
    })
    public SseEmitter subscribeUnblock(@RequestParam UUID userId) {
        return sseEmitterManager.addEmitter(userId);
    }
}