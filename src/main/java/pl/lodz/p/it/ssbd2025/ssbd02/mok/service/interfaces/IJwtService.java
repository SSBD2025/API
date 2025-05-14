package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces;

import jakarta.validation.constraints.NotNull;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.TokenPairDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;

import java.util.List;

public interface IJwtService {

     TokenPairDTO generatePair(@NotNull Account account, @NotNull List<String> roles);
     TokenPairDTO refresh(String token);
     boolean check(String token);
}
