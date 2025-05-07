package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.AdminService;

import java.util.List;

@AllArgsConstructor
@RestController
//@MethodCallLogged //todo!!
@RequestMapping(value = "/api/admin", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableMethodSecurity(prePostEnabled = true)
public class AdminController {

    private final AdminService adminService;
    private final AdminMapper adminMapper;
    private final AccountMapper accountMapper;
    private final UserRoleMapper userRoleMapper;

//    @PreAuthorize("hasRole('client_admin')")
    @PostMapping(value = "/register", consumes =  MediaType.APPLICATION_JSON_VALUE)
    public AdminDTO registerAdmin(@RequestBody @Validated(OnCreate.class) AdminDTO adminDTO) {
        Admin newAdminData = userRoleMapper.toNewAdminData(adminDTO.admin());
        Account newAccount = accountMapper.toNewAccount(adminDTO.account());
        return adminMapper.toAdminDTO(adminService.createAdmin(newAdminData, newAccount));
    }
    //activate dietician
    //create admin
    //activate admin

//    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AdminDTO> getAdminAccounts() {
        return adminService.getAdminAccounts();
    }
}
