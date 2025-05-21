package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.TokenPairDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;

import java.util.List;

public interface IJwtService {

     TokenPairDTO generatePair(@NotNull Account account, @NotNull List<String> roles);
     SensitiveDTO generateAccess(@NotNull Account account, @NotNull List<String> roles);
     SensitiveDTO generateShorterRefresh(@NotNull Account account, @NotNull List<String> roles);
     SensitiveDTO refresh(HttpServletRequest request, HttpServletResponse response);
     boolean check(String token);
}
