package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AdminDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.AccountMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.AdminMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.UserRoleMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Admin;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AppBaseException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.AuthorizedEndpoint;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IAdminService;

import java.util.List;

@AllArgsConstructor
@RestController
@MethodCallLogged
@RequestMapping(value = "/api/admin", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableMethodSecurity(prePostEnabled = true)
public class AdminController {

    private final IAdminService adminService;
    private final AdminMapper adminMapper;
    private final AccountMapper accountMapper;
    private final UserRoleMapper userRoleMapper;

    @PostMapping(value = "/register", consumes =  MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @AuthorizedEndpoint
    @Operation(summary = "Zarejestruj administratora", description = "Dostępne dla ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Konto administratora zostaje utworzone"),
            @ApiResponse(responseCode = "409", description = "Naruszenie ograniczeń - unikalność - login w użyciu"),
            @ApiResponse(responseCode = "409", description = "Naruszenie ograniczeń - unikalność - email w użyciu"),
    })
    public AdminDTO registerAdmin(@RequestBody @Validated(OnCreate.class) AdminDTO adminDTO) {
        Admin newAdminData = userRoleMapper.toNewAdminData(adminDTO.getAdmin());
        Account newAccount = accountMapper.toNewAccount(adminDTO.getAccount());
        return adminMapper.toAdminDTO(adminService.createAdmin(newAdminData, newAccount));
    }
}
