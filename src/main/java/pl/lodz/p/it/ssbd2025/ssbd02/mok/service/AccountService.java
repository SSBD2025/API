package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
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

    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();
        Account account = accountRepository.findByLogin(login);
        if (account != null && BCrypt.checkpw(changePasswordDTO.oldPassword(), account.getPassword())) {
            accountRepository.updatePassword(account.getLogin(), changePasswordDTO.newPassword());
        }
    }

    public UserDetails loadUserByUsername(String username) {
        Account user = accountRepository.findByLogin(username);
        if (user == null) { //TODO THIS NEEDS TO BE FIXED ASAP
            throw new AccountNotFoundException();
        }//ALERT ALERT
        return new UserPrincipal(user);
    }
}
