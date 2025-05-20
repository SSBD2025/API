package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IAccountService;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IJwtService;

import java.util.List;

import java.util.UUID;

import static pl.lodz.p.it.ssbd2025.ssbd02.utils.MiscellaneousUtil.getClientIp;

@RequiredArgsConstructor
@RestController
//@AllArgsConstructor
@MethodCallLogged
//@RequestMapping(value = "/api/account", produces = MediaType.APPLICATION_JSON_VALUE)
@RequestMapping(value = "/api/account")
@EnableMethodSecurity(prePostEnabled = true)
public class AccountController {

    @NonNull
    private final IAccountService accountService;
    private final IJwtService jwtService;

    @PostMapping(value = "/login", consumes =  MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public SensitiveDTO login(@RequestBody @Validated(OnCreate.class) LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        String ipAddress = getClientIp(request);
        return accountService.login(loginDTO.getLogin(), new SensitiveDTO(loginDTO.getPassword()), ipAddress, response);
    }

    @PostMapping(value = "/login/2fa", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('2FA_AUTHORITY')")
    public SensitiveDTO verifyTwoFactor(@RequestBody @Validated(OnRequest.class)TwoFactorDTO twoFactorDTO, HttpServletRequest request, HttpServletResponse response) {
        String ipAddress = getClientIp(request);
        return accountService.verifyTwoFactorCode(new SensitiveDTO(twoFactorDTO.getCode()), ipAddress, response);
    }

    @PostMapping(value = "/refresh")
    @PreAuthorize("permitAll()")
    public SensitiveDTO refresh(HttpServletRequest request, HttpServletResponse response) {
        return jwtService.refresh(request, response);
    }

    @MethodCallLogged
    @PostMapping("/changePassword")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public ResponseEntity<Object> changePassword(@RequestBody @Valid ChangePasswordDTO changePasswordDTO) {
        accountService.changePassword(changePasswordDTO);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @MethodCallLogged
    @PostMapping("/{id}/changePassword")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> changeUserPassword(@PathVariable UUID id) {
        accountService.setGeneratedPassword(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        accountService.logout(response);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public AccountWithTokenDTO getMe() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountService.getAccountByLogin(login);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public void updateMe(@RequestBody @Valid UpdateAccountDTO updateAccountDTO) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.updateMyAccount(login, updateAccountDTO);
    }

    @PostMapping("/change-email")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @MethodCallLogged
    public ResponseEntity<?> changeOwnEmail(@RequestBody @Valid ChangeEmailDTO changeEmailDTO) {
        accountService.changeOwnEmail(changeEmailDTO.getEmail());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/confirm-email")
    @PreAuthorize("permitAll()")
    @MethodCallLogged
    public ResponseEntity<?> confirmEmail(@RequestParam("token") SensitiveDTO token) {
        accountService.confirmEmail(token);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/revert-email-change")
    @PreAuthorize("permitAll()")
    @MethodCallLogged
    public ResponseEntity<?> revertEmailChange(@RequestParam("token") SensitiveDTO token) {
        accountService.revertEmailChange(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-change-email")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @MethodCallLogged
    public ResponseEntity<Void> resendEmailChangeLink() {
        accountService.resendEmailChangeLink();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/change-user-email")
    @PreAuthorize("hasRole('ADMIN')")
    @MethodCallLogged
    public ResponseEntity<?> changeUserEmail(
            @PathVariable UUID id,
            @RequestBody @Valid ChangeEmailDTO changeEmailDTO) {
        accountService.changeUserEmail(id, changeEmailDTO.getEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockAccount(@PathVariable UUID id) {
        accountService.blockAccount(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unblockAccount(@PathVariable UUID id) {
        accountService.unblockAccount(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @MethodCallLogged
    @PreAuthorize("permitAll()")
    @PostMapping("reset/password/request")
    public ResponseEntity<Void> resetPasswordRequest(@RequestBody @Validated(OnRequest.class) ResetPasswordDTO resetPasswordDTO) {
        accountService.sendResetPasswordEmail(resetPasswordDTO.getEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @MethodCallLogged
    @PreAuthorize("permitAll()")
    @PostMapping("reset/password/{token}")
    public ResponseEntity<Void> resetPassword(@PathVariable SensitiveDTO token, @RequestBody @Validated(OnReset.class) ResetPasswordDTO resetPasswordDTO) {
        accountService.resetPassword(token, resetPasswordDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AccountWithRolesDTO> getAllAccounts(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean verified,
            Pageable pageable
    ) {
        return accountService.getAllAccounts(active, verified, pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public AccountWithTokenDTO getAccountById(@PathVariable UUID id) {
        return accountService.getAccountById(id);
    }

    @GetMapping("/verify")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> verifyAccount(@RequestParam SensitiveDTO token) {
        accountService.verifyAccount(token);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateAccountById(@RequestBody @Valid UpdateAccountDTO updateAccountDTO,
                                                  @PathVariable UUID id) {
        accountService.updateAccountById(id, updateAccountDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/me/enable2f")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> enableTwoFactor() {
        accountService.enableTwoFactor();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/me/disable2f")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> disableTwoFactor() {
        accountService.disableTwoFactor();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/log-role-change")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    public ResponseEntity<Void> logRoleChange(@RequestBody @Valid RoleChangeLogDTO roleChangeLogDTO) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.logUserRoleChange(login, roleChangeLogDTO.getPreviousRole(), roleChangeLogDTO.getNewRole());
        return ResponseEntity.ok().build();
    }
}
