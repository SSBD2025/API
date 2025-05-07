package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;

import java.sql.Timestamp;
import java.util.UUID;

public record AccountReadDTO(
        UUID id,
        long version,
        String login,
        String email,
        boolean active,
        boolean verified,
        String firstName,
        String lastName,
        Timestamp lastSuccessfulLogin,
        Timestamp lastFailedLogin,
        Language language,
        String lastSuccessfulLoginIp,
        String lastFailedLoginIp
) {
}
