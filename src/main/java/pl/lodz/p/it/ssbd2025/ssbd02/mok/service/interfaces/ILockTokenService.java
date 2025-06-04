package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.util.UUID;

public interface ILockTokenService {
    SensitiveDTO generateToken(UUID id, Long version);
    LockTokenService.Record<UUID, Long> verifyToken(String token);
}
