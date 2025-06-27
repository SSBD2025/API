package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;

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
    public SseEmitter subscribeBlock(@RequestParam UUID userId) {
        return sseEmitterManager.addEmitter(userId);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/subscribe/unblock")
    public SseEmitter subscribeUnblock(@RequestParam UUID userId) {
            return sseEmitterManager.addEmitter(userId);
        }
}
