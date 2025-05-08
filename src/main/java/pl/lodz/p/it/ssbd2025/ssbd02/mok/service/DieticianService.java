package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.DieticianDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.AccountMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.DieticianMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.VerificationToken;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.DieticianRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.VerificationTokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
public class DieticianService {

    private final DieticianRepository dieticianRepository;
    private final AccountRepository accountRepository;
    private final DieticianMapper dieticianMapper;
    private final AccountMapper accountMapper;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${mail.verify.url}")
    private String verificationURL;


    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    public Dietician createDietician(Dietician newDietician, Account newAccount) {
        newAccount.setPassword(BCrypt.hashpw(newAccount.getPassword(), BCrypt.gensalt()));
        newDietician.setAccount(newAccount);
        newAccount.getUserRoles().add(newDietician);
        //the only difference between this and client is the fact that admin must manually activate the account
        Account createdAccount = accountRepository.saveAndFlush(newAccount);//todo check if this is correct
        String token = UUID.randomUUID().toString();
        emailService.sendActivationMail(newAccount.getEmail(), newAccount.getUsername(), verificationURL, newAccount.getLanguage(), token, false);
        System.out.println(verificationURL + token);
        verificationTokenRepository.saveAndFlush(new VerificationToken(token, createdAccount));
        return dieticianRepository.saveAndFlush(newDietician); //todo check if this is correct
    }

    public List<DieticianDTO> getDieticianAccounts() {
        Iterable<Dietician> dieticians = dieticianRepository.findAll();

        return StreamSupport.stream(dieticians.spliterator(), false)
                .map(dieticianMapper::toDieticianDTO)
                .collect(Collectors.toList());
    }

    public List<AccountDTO> getUnverifiedDieticianAccounts() {
        return accountRepository.findByActiveAndVerified(null, false).stream()
                .filter(acc -> acc.getUserRoles().stream().anyMatch(r -> r instanceof Dietician))
                .map(accountMapper::toAccountDTO)
                .collect(Collectors.toList());
    }
}