package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
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
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.DieticianRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
public class DieticianService {

    @NotNull
    private final DieticianRepository dieticianRepository;

    @NotNull
    private final AccountRepository accountRepository;

    private final DieticianMapper dieticianMapper;
    private final AccountMapper accountMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    public Dietician createDietician(Dietician newDietician, Account newAccount) {
        newAccount.setPassword(BCrypt.hashpw(newAccount.getPassword(), BCrypt.gensalt()));
        newDietician.setAccount(newAccount);
        newAccount.getUserRoles().add(newDietician);
        //the only difference between this and client is the fact that admin must manually activate the account
        accountRepository.saveAndFlush(newAccount); //todo check if this is correct
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