package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ChangePasswordDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.LoginDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.RefreshRequestDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.TokenPairDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.AccountService;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.JwtService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
//@AllArgsConstructor
//@RequestMapping(value = "/api/account", produces = MediaType.APPLICATION_JSON_VALUE)
@RequestMapping(value = "/api/account")
@EnableMethodSecurity(prePostEnabled = true)
public class AccountController {

    @NonNull
    private final AccountService accountService;
    private final JwtService jwtService;

//   @PostMapping("/changePassword") //TODO
//   public ResponseEntity<Object> changePassword(@RequestBody @Valid ChangePasswordDTO changePasswordDTO) {
//       accountService.changePassword(changePasswordDTO);
//       return ResponseEntity.status(HttpStatus.OK).build();
//   }

    @PostMapping(value = "/login", consumes =  MediaType.APPLICATION_JSON_VALUE)
    public TokenPairDTO login(@RequestBody @Validated(OnCreate.class) LoginDTO loginDTO, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        return accountService.login(loginDTO.getLogin(), loginDTO.getPassword(), ipAddress);
    }

//    @PreAuthorize("hasRole('CLIENT')||hasRole('DIETICIAN')||hasRole('ADMIN')")
    @PostMapping(value = "/refresh")
    public TokenPairDTO refresh(@RequestBody RefreshRequestDTO refreshRequestDTO){
        return jwtService.refresh(refreshRequestDTO.refreshToken());
    }

    @PostMapping("/logout") //this is the normal logout for our own security implementation
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public ResponseEntity<?> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        accountService.logout(token);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

//    @PreAuthorize("hasRole('CLIENT')||hasRole('client_admin')||hasRole('admin')")
//    @PreAuthorize("permitAll()")
//    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public String testClient() {
        return "Hello " + SecurityContextHolder.getContext().getAuthentication();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0]; // In case of multiple IPs
    }

    @PostMapping("/{id}/block")
    //@PreAuthorize()
    public ResponseEntity<Void> blockAccount(@PathVariable String id) {
        accountService.blockAccount(UUID.fromString(id));
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
