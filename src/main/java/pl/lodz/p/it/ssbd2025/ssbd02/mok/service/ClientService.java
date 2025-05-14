package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCrypt;
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
import pl.lodz.p.it.ssbd2025.ssbd02.entities.TokenEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.ClientRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.TokenUtil;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Service
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.REQUIRES_NEW,  readOnly = true, transactionManager = "mokTransactionManager")
public class ClientService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final ClientMapper clientMapper;
    private final AccountMapper accountMapper;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;
    private final TokenUtil tokenUtil;

    @Value("${mail.verify.url}")
    private String verificationURL;

    @Value("${account.verification.threshold}")
    private long accountVerificationThreshold;

    @PreAuthorize("permitAll()")
    @TransactionLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public Client createClient(Client newClient, Account newAccount) {
        newAccount.setPassword(BCrypt.hashpw(newAccount.getPassword(), BCrypt.gensalt()));
        newClient.setAccount(newAccount);
        newAccount.getUserRoles().add(newClient);
        newAccount.setActive(true); //verification is required anyway
        Account createdAccount = accountRepository.saveAndFlush(newAccount);
        String token = UUID.randomUUID().toString();
        emailService.sendActivationMail(newAccount.getEmail(), newAccount.getLogin(), verificationURL, newAccount.getLanguage(), token);
        tokenRepository.saveAndFlush(new TokenEntity(token, tokenUtil.generateHourExpiration(accountVerificationThreshold), createdAccount, TokenType.VERIFICATION));
        return clientRepository.saveAndFlush(newClient);
    }

    @TransactionLogged
    @Transactional(readOnly = true, transactionManager = "mokTransactionManager")
    public List<ClientDTO> getClientAccounts() {
        Iterable<Client> clients = clientRepository.findAll();

        return StreamSupport.stream(clients.spliterator(), false)
                .map(clientMapper::toClientDTO)
                .collect(Collectors.toList());
    }

    @TransactionLogged
    @Transactional(readOnly = true, transactionManager = "mokTransactionManager")
    public List<AccountDTO> getUnverifiedClientAccounts() {
        return accountRepository.findByActiveAndVerified(null, false).stream()
                .filter(acc -> acc.getUserRoles().stream()
                        .anyMatch(role -> role instanceof Client))
                .map(accountMapper::toAccountDTO)
                .collect(Collectors.toList());
    }
}
