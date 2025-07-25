package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
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
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.AuthorizedEndpoint;
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
    @Operation(summary = "Zaloguj się do systemu", description = "Dostępne dla wszystkich (także użytkowników nieuwierzytelnionych)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zwrócony zostaje żeton uwierzytelnienia"),
            @ApiResponse(responseCode = "401", description = "Zostały podane niepoprawne dane uwierzytelniające"),
            @ApiResponse(responseCode = "403", description = "Konto zostało zablokowane po zbyt długej nieaktywności"),
            @ApiResponse(responseCode = "403", description = "Niepoprawne hasło zostało podane zbyt wiele razy"),
            @ApiResponse(responseCode = "403", description = "Konto nie posiada żadnej roli"),
            @ApiResponse(responseCode = "403", description = "Konto nie jest aktywne"),
            @ApiResponse(responseCode = "403", description = "Konto nie jest zweryfikowane"),
            @ApiResponse(responseCode = "404", description = "Konto o podanym loginie nie istnieje"),
            @ApiResponse(responseCode = "428", description = "Wymagana jest zmiana hasła"),
    })
    public SensitiveDTO login(@RequestBody @Validated(OnCreate.class) LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        String ipAddress = getClientIp(request);
        return accountService.login(loginDTO.getLogin(), new SensitiveDTO(loginDTO.getPassword()), ipAddress, response);
    }

    @PostMapping(value = "/login/2fa", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('2FA_AUTHORITY')")
    @Operation(summary = "Weryfikacja kodu 2FA", description = "Weryfikuje kod dwuskładnikowy i zwraca żeton uwierzytelnienia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Poprawna weryfikacja"),
            @ApiResponse(responseCode = "400", description = "Niepoprawne dane wejściowe"),
            @ApiResponse(responseCode = "401", description = "Ważność żetonu wygasła"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta lub żetonu lub żeton jest niepoprawny"),
    })
    public SensitiveDTO verifyTwoFactor(@RequestBody @Validated(OnRequest.class)TwoFactorDTO twoFactorDTO, HttpServletRequest request, HttpServletResponse response) {
        String ipAddress = getClientIp(request);
        return accountService.verifyTwoFactorCode(new SensitiveDTO(twoFactorDTO.getCode()), ipAddress, response);
    }

    @PostMapping(value = "/refresh")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Odśwież żeton", description = "Dostępne dla wszystkich (także użytkowników nieuwierzytelnionych)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Żeton zostaje odświeżony"),
            @ApiResponse(responseCode = "403", description = "Podany został poprawny żeton ale o niepoprawnym typie"),
            @ApiResponse(responseCode = "404", description = "Ciasteczko nie zostało odnalezione"),
            @ApiResponse(responseCode = "404", description = "Żeton nie został odnaleziony"),
            @ApiResponse(responseCode = "404", description = "Żeton nie został odnaleziony"),
            @ApiResponse(responseCode = "403", description = "Konto nie posiada żadnej roli"),
    })
    public SensitiveDTO refresh(HttpServletRequest request, HttpServletResponse response) {
        return jwtService.refresh(request, response);
    }

    @PostMapping("/changePassword")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Operation(summary = "Zmień hasło własnego konta", description = "Dostępne dla ADMIN, CLIENT i DIETICIAN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Operacja powiodła się"),
            @ApiResponse(responseCode = "401", description = "Niepoprawne hasło"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta"),
            @ApiResponse(responseCode = "400", description = "Hasło używane wcześniej")
    })
    public ResponseEntity<Object> changePassword(@RequestBody @Valid ChangePasswordDTO changePasswordDTO) {
        accountService.changePassword(changePasswordDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/force/changePassword")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Zmień hasło po pierwszym uwierzytelnieniu lub po zmianie hasła przez administratora",
            description = "Dostępne dla wszystkich")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Operacja powiodła się"),
            @ApiResponse(responseCode = "401", description = "Niepoprawne hasło"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta"),
    })
    public ResponseEntity<Object> forceChangePassword(@RequestBody @Valid ForceChangePasswordDTO forceChangePasswordDTO) {
        accountService.forceChangePassword(forceChangePasswordDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/changePassword")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Zmień hasło użytkownikowi", description = "Dostępne dla ADMIN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Operacja powiodła się"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta"),
    })
    public ResponseEntity<Object> changeUserPassword(@PathVariable UUID id) {
        accountService.setGeneratedPassword(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @AuthorizedEndpoint
    @Operation(summary = "Wyloguj się z systemu", description = "Dostępne dla ADMIN, CLIENT i DIETICIAN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Użytkownik zostaje wylogowany"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta o podanym loginie")
    })
    public ResponseEntity<?> logout(HttpServletResponse response) {
        accountService.logout(response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Operation(summary = "Pobierz dane zalogowanego użytkownika", description = "Dostępne dla ADMIN, CLIENT i DIETICIAN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dane użytkownika zostały poprawnie zwrócone"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta o podanym loginie")
    })
    public AccountWithTokenDTO getMe() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountService.getAccountByLogin(login);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Operation(summary = "Zaktualizuj dane zalogowanego użytkownika", description = "Dostępne dla ADMIN, CLIENT i DIETICIAN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dane użytkownika zostały zaktualizowane"),
            @ApiResponse(responseCode = "403", description = "Konto nie jest aktywne"),
            @ApiResponse(responseCode = "403", description = "Niepoprawny lock token"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta o podanym loginie"),
            @ApiResponse(responseCode = "409", description = "Jednoczesna zmiana zasobu")
    })
    public void updateMe(@RequestBody @Valid UpdateAccountDTO updateAccountDTO) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.updateMyAccount(login, updateAccountDTO);
    }

    @PostMapping("/change-email")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @MethodCallLogged
    @Operation(summary = "Zmień własny adres e-mail",
            description = "Dostępne dla ADMIN, CLIENT i DIETICIAN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Wysłano e-mail z linkiem do potwierdzenia zmiany zmiany adresu e-mail"),
            @ApiResponse(responseCode = "400", description = "Niepoprawne dane wejściowe"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta"),
            @ApiResponse(responseCode = "409", description = "Adres e-mail jest już w użyciu"),
    })
    public ResponseEntity<?> changeOwnEmail(@RequestBody @Valid ChangeEmailDTO changeEmailDTO) {
        accountService.changeOwnEmail(changeEmailDTO.getEmail());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/confirm-email")
    @PreAuthorize("permitAll()")
    @MethodCallLogged
    @Operation(summary = "Potwierdź zmianę adresu e-mail",
            description = "Dostępne dla wszystkich")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Zmieniono adres e-mail i wysłano wiadomość e-mail z linkiem do przywrócenia wcześniejszego adresu e-mail na stary adres e-mail"),
            @ApiResponse(responseCode = "400", description = "Niepoprawne dane wejściowe"),
            @ApiResponse(responseCode = "401", description = "Token do zmiany adresu e-mail wygasł"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta; nie znaleziono żetonu")
    })
    public ResponseEntity<?> confirmEmail(@RequestParam("token") SensitiveDTO token) {
        accountService.confirmEmail(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/revert-email-change")
    @PreAuthorize("permitAll()")
    @MethodCallLogged
    @Operation(summary = "Przywróć stary adres e-mail",
            description = "Dostępne dla wszystkich")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Przywrócono stary adres e-mail"),
            @ApiResponse(responseCode = "400", description = "Niepoprawne dane wejściowe"),
            @ApiResponse(responseCode = "401", description = "Token do zmiany adresu e-mail wygasł"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta; nie znaleziono żetonu")
    })
    public ResponseEntity<?> revertEmailChange(@RequestParam("token") SensitiveDTO token) {
        accountService.revertEmailChange(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-change-email")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @MethodCallLogged
    @AuthorizedEndpoint
    @Operation(summary = "Przywróć stary adres e-mail",
            description = "Dostępne dla ADMIN, CLIENT i DIETICIAN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Przywrócono stary adres e-mail"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta; nie znaleziono żetonu")
    })
    public ResponseEntity<Void> resendEmailChangeLink() {
        accountService.resendEmailChangeLink();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/change-user-email")
    @PreAuthorize("hasRole('ADMIN')")
    @MethodCallLogged
    @Operation(summary = "Zmień własny adres e-mail",
            description = "Dostępne dla ADMIN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Wysłano e-mail z linkiem do potwierdzenia zmiany zmiany"),
            @ApiResponse(responseCode = "400", description = "Niepoprawne dane wejściowe"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta"),
            @ApiResponse(responseCode = "409", description = "Adres e-mail jest już w użyciu"),
    })
    public ResponseEntity<?> changeUserEmail(
            @PathVariable UUID id,
            @RequestBody @Valid ChangeEmailDTO changeEmailDTO) {
        accountService.changeUserEmail(id, changeEmailDTO.getEmail());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Zablokuj konto o podanym id",
            description = "Dostępne dla ADMIN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Konto o podanym id zostało pomyślnie zablokowane"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta o podanym id"),
            @ApiResponse(responseCode = "403", description = "Nie można zablokować własnego konta"),
            @ApiResponse(responseCode = "409", description = "Konto o podanym id jest już zablokowane")
    })
    public ResponseEntity<Void> blockAccount(@PathVariable UUID id) {
        accountService.blockAccount(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Odblokuj konto o podanym id",
            description = "Dostępne dla ADMIN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Konto o podanym id zostało pomyślnie odblokowane"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta o podanym id"),
            @ApiResponse(responseCode = "409", description = "Konto o podanym id jest już odblokowane")
    })
    public ResponseEntity<Void> unblockAccount(@PathVariable UUID id) {
        accountService.unblockAccount(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/reset/password/request")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Otrzymaj link do resetu hasła na podany adres e-mail", description = "Dostępne dla wszystkich")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Operacja powiodła się, podano poprawny e-mail"),
            @ApiResponse(responseCode = "204", description = "Operacja nie powiodła się, podano niepoprawny e-mail"),
    })
    public ResponseEntity<Void> resetPasswordRequest(@RequestBody @Validated(OnRequest.class) ResetPasswordDTO resetPasswordDTO) {
        accountService.sendResetPasswordEmail(resetPasswordDTO.getEmail());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/reset/password/{token}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Ustawienie nowego hasła po resecie", description = "Dostępne dla wszystkich")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Operacja powiodła się"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono tokenu"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta"),
            @ApiResponse(responseCode = "401", description = "Token wygasł"),
            @ApiResponse(responseCode = "400", description = "Nowe hasło było już używane"),

    })
    public ResponseEntity<Void> resetPassword(@PathVariable SensitiveDTO token, @RequestBody @Validated(OnReset.class) ResetPasswordDTO resetPasswordDTO) {
        accountService.resetPassword(token, resetPasswordDTO);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Pobierz listę kont z rólami",
            description = "Dostępne tylko dla administratora. Możliwość filtrowania po statusie aktywności, weryfikacji oraz frazie wyszukiwania.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zwrócono stronę z listą kont"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe parametry zapytania")
    })
    public ResponseEntity<PagedModel<EntityModel<AccountWithRolesDTO>>> getAllAccounts(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) String searchPhrase,
            Pageable pageable,
            PagedResourcesAssembler<AccountWithRolesDTO> pagedAssembler
    ) {
        Page<AccountWithRolesDTO> page = accountService.getAllAccounts(active, verified, searchPhrase, pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(page));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Pobierz dane użytkownika po ID", description = "Dostępne dla ADMIN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dane użytkownika zostały poprawnie zwrócone"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta o podanym id")
    })
    public AccountWithTokenDTO getAccountById(@PathVariable UUID id) {
        return accountService.getAccountById(id);
    }

    @GetMapping("/verify")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Weryfikacja konta", description = "Dostępne dla wszystkich (także użytkowników nieuwierzytelnionych)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Konto zostaje zweryfikowane"),
            @ApiResponse(responseCode = "401", description = "Żeton wygasł"),
            @ApiResponse(responseCode = "404", description = "Żeton nie został odnaleziony"),
            @ApiResponse(responseCode = "404", description = "Konto nie zostało odnalezione"),
            @ApiResponse(responseCode = "409", description = "Konto już zostało zweryfikowane"),
    })
    public ResponseEntity<Void> verifyAccount(@RequestParam SensitiveDTO token) {
        accountService.verifyAccount(token);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Zaktualizuj dane użytkownika po ID", description = "Dostępne dla ADMIN")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dane użytkownika zostały zaktualizowane"),
            @ApiResponse(responseCode = "403", description = "Konto nie jest aktywne"),
            @ApiResponse(responseCode = "403", description = "Niepoprawny lock token"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta o podanym loginie"),
            @ApiResponse(responseCode = "409", description = "Jednoczesna zmiana zasobu")
    })
    public ResponseEntity<Void> updateAccountById(@RequestBody @Valid UpdateAccountDTO updateAccountDTO,
                                                  @PathVariable UUID id) {
        accountService.updateAccountById(id, updateAccountDTO);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/me/enable2f")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Włączenie dwuskładnikowego uwierzytelniania",
            description = "Dostępne dla uwierzytelnionego użytkownika")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Dwuskładnikowe uwierzytelnianie zostało pomyślnie włączone"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta"),
            @ApiResponse(responseCode = "409", description = "Dwuskładnikowe uwierzytelnianie było wcześniej włączone")
    })
    public ResponseEntity<Void> enableTwoFactor() {
        accountService.enableTwoFactor();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/me/disable2f")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Wyłączenie dwuskładnikowego uwierzytelniania",
            description = "Dostępne dla uwierzytelnionego użytkownika")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Dwuskładnikowe uwierzytelnianie zostało pomyślnie wyłączone"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta"),
            @ApiResponse(responseCode = "409", description = "Dwuskładnikowe uwierzytelnianie było wcześniej wyłączone")
    })
    public ResponseEntity<Void> disableTwoFactor() {
        accountService.disableTwoFactor();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/log-role-change")
    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Operation(summary = "Zaloguj zmianę roli użytkownika",
            description = "Dostępne dla użytkowników posiadających rolę ADMIN, CLIENT lub DIETICIAN. Służy do rejestracji zmiany roli po stronie klienta.")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Zalogowano zmianę roli pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta użytkownika"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień do wykonania operacji")
    })
    public ResponseEntity<Void> logRoleChange(@RequestBody @Valid RoleChangeLogDTO roleChangeLogDTO) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.logUserRoleChange(login, roleChangeLogDTO.getPreviousRole(), roleChangeLogDTO.getNewRole());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unlock")
    @PreAuthorize("permitAll()")
    @MethodCallLogged
    @Operation(summary = "Odblokowanie konta po 30 dniach nieaktywności",
            description = "Dostępne dla nieuwierzytelnionego użytkownika")
    @ApiResponses(value =  {
            @ApiResponse(responseCode = "204", description = "Konto zostało pomyślnie odblokowane"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono żetonu"),
            @ApiResponse(responseCode = "401", description = "Skończył się czas ważnosci żetonu"),

    })
    public ResponseEntity<?> unlockRequest(@RequestParam SensitiveDTO token) {
        accountService.unlockAccount(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/auth/email/request")
    @PreAuthorize("permitAll()")
    @MethodCallLogged
    public ResponseEntity<?> authWithEmailRequest(@RequestBody @Valid ChangeEmailDTO changeEmailDTO) {
        accountService.authWithEmailRequest(changeEmailDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/auth/email")
    @PreAuthorize("permitAll()")
    @MethodCallLogged
    @Operation(summary = "Uwierzytelnianie konta za pomocą kodu e-mail",
            description = "Dostępne publicznie. Loguje użytkownika na podstawie ważnego kodu przesłanego e-mailem.")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uwierzytelnienie zakończone sukcesem, zwrócono token dostępu"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta lub tokenu"),
            @ApiResponse(responseCode = "403", description = "Konto nieaktywne, niezweryfikowane lub nieposiadające ról"),
            @ApiResponse(responseCode = "409", description = "Brak przypisanych ról aktywnych")
    })
    public ResponseEntity<?> authWithEmail(@RequestBody @Valid SensitiveDTO code, HttpServletRequest request, HttpServletResponse response) {
        String ipAddress = getClientIp(request);
        return ResponseEntity.status(HttpStatus.OK).body(accountService.authWithEmail(code, ipAddress, response));
    }
}
