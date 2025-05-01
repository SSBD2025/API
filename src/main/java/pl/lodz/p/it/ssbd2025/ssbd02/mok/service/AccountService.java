package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ChangePasswordDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.UserRole;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.UserPrincipal;

@Component
@RequiredArgsConstructor
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
public class AccountService {

    @NotNull
    private final AccountRepository accountRepository;
//
//    public void changePassword(ChangePasswordDTO changePasswordDTO) {
//        //todo dodać login użytkownika odczytywany z tokenu
//        Account account = accountRepository.findByLogin("TODO");
//        if (true) { //TODO to trzeba będzie za pomocą keycloak zrobić
////            BCrypt.checkpw(changePasswordDTO.oldPassword(), account.getPassword())
//            accountRepository.updatePassword(account.getLogin(), changePasswordDTO.newPassword());
//        }
//    }

    public UserDetails loadUserByUsername(String username) {
        Account user = accountRepository.findByLogin(username);
        if (user == null) { //TODO THIS NEEDS TO BE FIXED ASAP
            throw new AccountNotFoundException();
        }//ALERT ALERT
        return new UserPrincipal(user);
    }
}
