package pl.lodz.p.it.ssbd2025.ssbd02.mok.rest;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.AccountMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.ClientMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.UserRoleMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.ClientService;

//@RequiredArgsConstructor
//@AllArgsConstructor
@RestController
//@MethodCallLogged //todo!!
//@RequestMapping(value = "/api/client", produces = MediaType.APPLICATION_JSON_VALUE)
@RequestMapping("/api/client")
@EnableMethodSecurity(prePostEnabled = true)
public class ClientController {

    private final ClientService clientService;

    private final ClientMapper clientMapper;

    private final AccountMapper accountMapper;

    private final UserRoleMapper userRoleMapper;

    public ClientController(ClientService clientService, ClientMapper clientMapper, AccountMapper accountMapper, UserRoleMapper userRoleMapper) {
        this.clientService = clientService;
        this.clientMapper = clientMapper;
        this.accountMapper = accountMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @PostMapping(value = "/register", consumes =  MediaType.APPLICATION_JSON_VALUE)
    public ClientDTO registerClient(@RequestBody @Validated(OnCreate.class) ClientDTO clientDTO) {
        Client newClientData = userRoleMapper.toNewClientData(clientDTO.client());
        Account newAccount = accountMapper.toNewAccount(clientDTO.account());
        return clientMapper.toClientDTO(clientService.createClient(newClientData, newAccount));
    }
}
