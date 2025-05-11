package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.AccountMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.ClientMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.UserRoleMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AppBaseException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.ClientService;

import java.util.List;

@AllArgsConstructor
@RestController
@MethodCallLogged
@RequestMapping(value = "/api/client", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableMethodSecurity(prePostEnabled = true)
public class ClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;
    private final AccountMapper accountMapper;
    private final UserRoleMapper userRoleMapper;

    @PostMapping(value = "/register", consumes =  MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ClientDTO registerClient(@RequestBody @Validated(OnCreate.class) ClientDTO clientDTO) {
        Client newClientData = userRoleMapper.toNewClientData(clientDTO.client());
        Account newAccount = accountMapper.toNewAccount(clientDTO.account());
        return clientMapper.toClientDTO(clientService.createClient(newClientData, newAccount));
    }

    @PreAuthorize("hasRole('ADMIN')||hasRole('DIETICIAN')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ClientDTO> getClientAccounts() throws AppBaseException {
        return clientService.getClientAccounts();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/unverified")
    @ResponseStatus(HttpStatus.OK)
    public List<AccountDTO> getUnverifiedClientAccounts() throws AppBaseException {
        return clientService.getUnverifiedClientAccounts();
    }
}
