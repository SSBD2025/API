package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.AccountService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;

@RequiredArgsConstructor
@RestController
//@AllArgsConstructor
//@RequestMapping(value = "/api/account", produces = MediaType.APPLICATION_JSON_VALUE)
@RequestMapping(value = "/api/account")
@EnableMethodSecurity(prePostEnabled = true)
public class AccountController {

    @NonNull
    private final AccountService accountService;

//   @PostMapping("/changePassword") //TODO
//   public ResponseEntity<Object> changePassword(@RequestBody @Valid ChangePasswordDTO changePasswordDTO) {
//       accountService.changePassword(changePasswordDTO);
//       return ResponseEntity.status(HttpStatus.OK).build();
//   }

    @PostMapping(value = "/login", consumes =  MediaType.APPLICATION_JSON_VALUE)
    public String login(@RequestBody @Validated(OnCreate.class) LoginDTO loginDTO) {
        return accountService.login(loginDTO.getLogin(), loginDTO.getPassword());
    }

//    @PreAuthorize("hasRole('CLIENT')||hasRole('client_admin')||hasRole('admin')")
//    @PreAuthorize("permitAll()")
//    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public String testClient() {
        return "Hello " + SecurityContextHolder.getContext().getAuthentication();
    }

}