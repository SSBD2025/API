package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountRolesProjection;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountWithRolesDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.UserRole;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AccountNotActiveException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AccountNotVerifiedException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.InvalidCredentialsException;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ChangePasswordDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.UserPrincipal;

@Component
@RequiredArgsConstructor
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
public class AccountService {

    @NotNull
    private final AccountRepository accountRepository;
    @NotNull
    private final JwtUtil jwtUtil;
    @NotNull
    private final JwtTokenProvider jwtTokenProvider;
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
        Account account = accountRepository.findByLogin(username);
        if(account == null) {
            throw new AccountNotFoundException();
        } else {
            return account;
        }
    }

    public String login(String username, String password) { //todo 90% sure its not correct
        Account account = accountRepository.findByLogin(username);
        List<AccountRolesProjection> roles = accountRepository.findAccountRolesByLogin(username);
        List<String> userRoles = new ArrayList<>();
        roles.forEach(role -> {
            if (role.isActive()) { //todo this needs to be fixed later to support dynamic role granting
                userRoles.add(role.getRoleName());
            }
        });
        if (account == null) {
            throw new AccountNotFoundException();
        }
//        if (!account.isActive()) {
//            throw new AccountNotActiveException();
//        } //todo uncomment later
//        if (!account.isVerified()) {
//            throw new AccountNotVerifiedException();
//        }
        if (jwtUtil.checkPassword(password, account.getPassword())) {
            return jwtTokenProvider.generateToken(account, userRoles);
        } else {
            throw new InvalidCredentialsException();
        }
    }

    public String logout(String token) {
        jwtUtil.invalidateToken(token);
        SecurityContextHolder.clearContext();
        return "User logged out successfully";
    }

    public String me() {
        return "Hello " + SecurityContextHolder.getContext().getAuthentication().getName();
    }
//    public void changePassword(ChangePasswordDTO changePasswordDTO) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String login = auth.getName();
//        Account account = accountRepository.findByLogin(login);
//        if (account != null && BCrypt.checkpw(changePasswordDTO.oldPassword(), account.getPassword())) {
//            accountRepository.updatePassword(account.getLogin(), changePasswordDTO.newPassword());
//        }
//    }
//
//    public UserDetails loadUserByUsername(String username) {
//        Account user = accountRepository.findByLogin(username);
//        if (user == null) { //TODO THIS NEEDS TO BE FIXED ASAP
//            throw new AccountNotFoundException();
//        }//ALERT ALERT
//        return new UserPrincipal(user);
//    }
}
