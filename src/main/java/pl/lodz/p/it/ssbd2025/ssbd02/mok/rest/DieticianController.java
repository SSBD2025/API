package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.DieticianDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.AccountMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.DieticianMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.UserRoleMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.DieticianService;

@AllArgsConstructor
@RestController
//@MethodCallLogged //todo!!
@RequestMapping(value = "/api/dietician", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableMethodSecurity(prePostEnabled = true)
public class DieticianController {

    private final DieticianService dieticianService;
    private final DieticianMapper dieticianMapper;
    private final AccountMapper accountMapper;
    private final UserRoleMapper userRoleMapper;

    @PostMapping(value = "/register", consumes =  MediaType.APPLICATION_JSON_VALUE)
    public DieticianDTO registerDietician(@RequestBody @Validated(OnCreate.class) DieticianDTO dieticianDTO) {
        Dietician newDieticianData = userRoleMapper.toNewDietician(dieticianDTO.dietician());
        Account newAccount = accountMapper.toNewAccount(dieticianDTO.account());
        return dieticianMapper.toDieticianDTO(dieticianService.createDietician(newDieticianData, newAccount));
    }
}
