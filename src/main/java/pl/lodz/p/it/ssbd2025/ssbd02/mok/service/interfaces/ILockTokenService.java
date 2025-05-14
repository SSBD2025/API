package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations.LockTokenService;

import java.util.UUID;

public interface ILockTokenService {
    String generateToken(UUID id, Long version);
    LockTokenService.Record<UUID, Long> verifyToken(String token);
}
