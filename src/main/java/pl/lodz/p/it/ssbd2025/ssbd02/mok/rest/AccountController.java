package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ChangeEmailDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ChangePasswordDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.LoginDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRequest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnReset;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.AccountService;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.JwtService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

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

    @PostMapping("/changePassword")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public ResponseEntity<Object> changePassword(@RequestBody @Valid ChangePasswordDTO changePasswordDTO) {
        accountService.changePassword(changePasswordDTO);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/changePassword")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> changeUserPassword(@PathVariable String id) {
        String password = accountService.setGeneratedPassword(UUID.fromString(id));
        return ResponseEntity.status(HttpStatus.OK).body(password); //TODO zastanowić się, czy zwracać tu tę wartość
    }

    @PostMapping("/logout") //this is the normal logout for our own security implementation
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public ResponseEntity<?> logout() {
        accountService.logout();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //    @PreAuthorize("hasRole('CLIENT')||hasRole('client_admin')||hasRole('admin')")
//    @PreAuthorize("permitAll()")
//    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public AccountWithTokenDTO getMe() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountService.getAccountByLogin(login);
    }

    @PutMapping("/me")
    public void updateMe(@RequestBody @Valid UpdateAccountDTO updateAccountDTO) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.updateMyAccount(login, updateAccountDTO);
    }
    
    @PostMapping("/change-email")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public ResponseEntity<?> changeEmail(@RequestBody @Valid ChangeEmailDTO changeEmailDTO) {
        accountService.changeOwnEmail(changeEmailDTO.email());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<?> confirmEmail(@RequestParam("token") String token ) {
        accountService.confirmEmail(token);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/revert-email-change")
    public ResponseEntity<?> revertEmailChange(@RequestParam("token") String token) {
        accountService.revertEmailChange(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-change-email")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public ResponseEntity<Void> resendEmailChangeLink() {
        accountService.resendEmailChangeLink();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/change-user-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeUserEmail(
            @PathVariable String id,
            @RequestBody @Valid ChangeEmailDTO changeEmailDTO) {
        accountService.changeUserEmail(UUID.fromString(id), changeEmailDTO.email());
        return new ResponseEntity<>(HttpStatus.OK);
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

    @PostMapping("reset/password/request")
    public ResponseEntity<Void> resetPasswordRequest(@RequestBody @Validated(OnRequest.class) ResetPasswordDTO resetPasswordDTO) {
        accountService.sendResetPasswordEmail(resetPasswordDTO.email());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("reset/password/{token}")
    public ResponseEntity<Void> resetPassword(@PathVariable String token, @RequestBody @Validated(OnReset.class) ResetPasswordDTO resetPasswordDTO) {
        accountService.resetPassword(token, resetPasswordDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AccountDTO> getAllAccounts(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean verified
    ) {
        return accountService.getAllAccounts(active, verified);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public AccountWithTokenDTO getAccountById(@PathVariable String id) {
        return accountService.getAccountById(id);
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyAccount(@RequestParam String token) {
        accountService.verifyAccount(token);
//        try {
//            response.sendRedirect("/login");
//        } catch (IOException e) {
//            response.setStatus(HttpStatus.OK.value());
//        }
//        response.setStatus(HttpStatus.OK.value());
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateAccountById(@RequestBody @Valid UpdateAccountDTO updateAccountDTO,
                                                  @PathVariable String id) {
        accountService.updateAccountById(id, updateAccountDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
