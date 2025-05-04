package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.ClientRepository;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    public Client createClient(Client newClient, Account newAccount) {
        newAccount.setPassword(BCrypt.hashpw(newAccount.getPassword(), BCrypt.gensalt()));
        newClient.setAccount(newAccount);
        newAccount.getUserRoles().add(newClient);
        newAccount.setActive(true); //todo can this stay? verification is required anyway
        accountRepository.saveAndFlush(newAccount);//todo check if this is correct
        return clientRepository.saveAndFlush(newClient);//todo check if this is correct
    }

}