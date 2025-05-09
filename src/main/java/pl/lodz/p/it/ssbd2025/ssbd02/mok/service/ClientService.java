package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import pl.lodz.p.it.ssbd2025.ssbd02.entities.VerificationToken;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.ClientRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.VerificationTokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Service
//@MethodCallLogged //todo!!
@Transactional(propagation = Propagation.REQUIRES_NEW,  readOnly = true, transactionManager = "mokTransactionManager")
public class ClientService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final ClientMapper clientMapper;
    private final AccountMapper accountMapper;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${mail.verify.url}")
    private String verificationURL;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    public Client createClient(Client newClient, Account newAccount) {
        newAccount.setPassword(BCrypt.hashpw(newAccount.getPassword(), BCrypt.gensalt()));
        newClient.setAccount(newAccount);
        newAccount.getUserRoles().add(newClient);
        newAccount.setActive(true); //todo can this stay? verification is required anyway
        Account createdAccount = accountRepository.saveAndFlush(newAccount);//todo check if this is correct
        String token = UUID.randomUUID().toString();
        emailService.sendActivationMail(newAccount.getEmail(), newAccount.getLogin(), verificationURL, newAccount.getLanguage(), token, false);
        System.out.println(verificationURL + token);
        verificationTokenRepository.saveAndFlush(new VerificationToken(token, createdAccount));
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
