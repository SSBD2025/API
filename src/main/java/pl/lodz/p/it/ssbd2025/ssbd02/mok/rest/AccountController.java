package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ChangePasswordDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.AccountService;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/account", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccountController {

    @NotNull
    private final AccountService accountService;

    @PostMapping("/changePassword")
    public ResponseEntity<Object> changePassword(@RequestBody @Valid ChangePasswordDTO changePasswordDTO) {
        accountService.changePassword(changePasswordDTO);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
