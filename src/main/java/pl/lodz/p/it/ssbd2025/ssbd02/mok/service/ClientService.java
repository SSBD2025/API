package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.AccountMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.ClientMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.ClientRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Service
//@MethodCallLogged //todo!!
@Transactional(propagation = Propagation.REQUIRES_NEW,  readOnly = true, transactionManager = "mokTransactionManager")
public class ClientService {

    @NotNull
    private final ClientRepository clientRepository;

    @NotNull
    private final AccountRepository accountRepository;

    private final ClientMapper clientMapper;
    private final AccountMapper accountMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    public Client createClient(Client newClient, Account newAccount) {
        newAccount.setPassword(BCrypt.hashpw(newAccount.getPassword(), BCrypt.gensalt()));
        newClient.setAccount(newAccount);
        newAccount.getUserRoles().add(newClient);
        newAccount.setActive(true); //todo can this stay? verification is required anyway
        accountRepository.saveAndFlush(newAccount);//todo check if this is correct
        return clientRepository.saveAndFlush(newClient);//todo check if this is correct
    }

    public List<ClientDTO> getClientAccounts() {
        Iterable<Client> clients = clientRepository.findAll();

        return StreamSupport.stream(clients.spliterator(), false)
                .map(clientMapper::toClientDTO)
                .collect(Collectors.toList());
    }

    public List<AccountDTO> getUnverifiedClientAccounts() {
        return accountRepository.findByActiveAndVerified(null, false).stream()
                .filter(acc -> acc.getUserRoles().stream()
                        .anyMatch(role -> role instanceof Client))
                .map(accountMapper::toAccountDTO)
                .collect(Collectors.toList());
    }
}
