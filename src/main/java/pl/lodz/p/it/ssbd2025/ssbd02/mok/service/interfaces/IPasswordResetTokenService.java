package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;

public interface IPasswordResetTokenService {

    void createPasswordResetToken(Account account, SensitiveDTO token);
    void validatePasswordResetToken(SensitiveDTO passwordResetToken);
}
