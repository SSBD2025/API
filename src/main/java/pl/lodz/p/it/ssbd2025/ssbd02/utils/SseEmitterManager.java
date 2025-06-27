package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

@MethodCallLogged
@Component
public class SseEmitterManager {
    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter addEmitter(UUID userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));
        return emitter;
    }

    private void removeEmitter(UUID userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }

    public void sendBlockedNotification(UUID userId) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) return;

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("account-blocked")
                        .data(""));
            } catch (IOException e) {
                emitter.completeWithError(e);
                removeEmitter(userId, emitter);
            }
        }
    }

    public void sendUnblockedNotification(UUID userId) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) return;

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("account-unblocked")
                        .data(""));
            } catch (IOException e) {
                emitter.completeWithError(e);
                removeEmitter(userId, emitter);
            }
        }
    }
}