package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
import pl.lodz.p.it.ssbd2025.ssbd02.dto.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountRolesProjection;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.AccountMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.InvalidCredentialsException;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.UserRole;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AccountNotActiveException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AccountNotVerifiedException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.InvalidCredentialsException;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.*;

import java.security.Timestamp;
import java.util.*;

import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtUtil;

import java.util.*;

import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtUtil;
import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Optional;
import java.util.UUID;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.ChangePasswordDTO;

@Component
@RequiredArgsConstructor
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
public class AccountService {

    @NotNull
    private final AccountRepository accountRepository;
    @NotNull
    private final JwtUtil jwtUtil;
    @NotNull
    private final JwtTokenProvider jwtTokenProvider;
    @NotNull
    private final EmailService emailService;
    @NotNull
    private final JwtService jwtService;
    @NotNull
    private final AccountMapper accountMapper;

    public UserDetails loadUserByUsername(String username) {
        Account account = accountRepository.findByLogin(username);
        if(account == null) {
            throw new AccountNotFoundException();
        } else {
            return account;
        }
    }


    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByLogin(login);
        if(account == null) {
            throw new AccountNotFoundException();
        }
        if(!BCrypt.checkpw(changePasswordDTO.oldPassword(), account.getPassword())) {
            throw new InvalidCredentialsException();
        }
        accountRepository.updatePassword(account.getLogin(), BCrypt.hashpw(changePasswordDTO.newPassword(), BCrypt.gensalt()));
    }


    public TokenPairDTO login(String username, String password, String ipAddress) { //todo 90% sure its not correct
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
        Date currentTime = new Date(System.currentTimeMillis());
        if (jwtUtil.checkPassword(password, account.getPassword())) {
            accountRepository.updateSuccessfulLogin(username, currentTime, ipAddress);
            return jwtService.generatePair(account, userRoles);
        } else {
            accountRepository.updateFailedLogin(username, currentTime, ipAddress);
            throw new InvalidCredentialsException();
        }
    }

    public void logout(String token) {
        jwtUtil.invalidateToken(token);
        SecurityContextHolder.clearContext();
    }

    //@Retryable
    public void blockAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException());

        if (!account.isActive())
            throw new AccountNotActiveException(); //TODO zmienic

        //TODO obsluga wyjatku

        account.setActive(false);
        accountRepository.saveAndFlush(account);

        emailService.sendBlockAccountEmail(account.getEmail(), account.getUsername(), account.getLanguage());
        //TODO logowanie

    }

    public String me() {
        return "Hello " + SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public List<AccountDTO> getAllAccounts(Boolean active, Boolean verified) {
        List<Account> accounts = accountRepository.findByActiveAndVerified(active, verified);

        return accounts.stream()
                .map(accountMapper::toAccountDTO)
                .collect(Collectors.toList());
    }
}
