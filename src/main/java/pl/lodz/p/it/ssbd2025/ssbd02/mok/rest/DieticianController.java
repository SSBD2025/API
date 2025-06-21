package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.DieticianDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.AccountMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.DieticianMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.UserRoleMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AppBaseException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IDieticianService;

import java.util.List;

@AllArgsConstructor
@RestController
@MethodCallLogged
@RequestMapping(value = "/api/dietician", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableMethodSecurity(prePostEnabled = true)
public class DieticianController {

    private final IDieticianService dieticianService;
    private final DieticianMapper dieticianMapper;
    private final AccountMapper accountMapper;
    private final UserRoleMapper userRoleMapper;

    @PostMapping(value = "/register", consumes =  MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @Operation(summary = "Zarejestruj się jako dietetyk", description = "Dostępne dla wszystkich (także użytkowników nieuwierzytelnionych)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Konto dietetyka zostaje utworzone"),
            @ApiResponse(responseCode = "409", description = "Naruszenie ograniczeń - unikalność - login w użyciu"),
            @ApiResponse(responseCode = "409", description = "Naruszenie ograniczeń - unikalność - email w użyciu"),
    })
    public DieticianDTO registerDietician(@RequestBody @Validated(OnCreate.class) DieticianDTO dieticianDTO) {
        Dietician newDieticianData = userRoleMapper.toNewDietician(dieticianDTO.getDietician());
        Account newAccount = accountMapper.toNewAccount(dieticianDTO.getAccount());
        return dieticianMapper.toDieticianDTO(dieticianService.createDietician(newDieticianData, newAccount));
    }
}
