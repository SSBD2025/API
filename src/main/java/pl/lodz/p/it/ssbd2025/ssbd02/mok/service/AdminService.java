package pl.lodz.p.it.ssbd2025.ssbd02.mok.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AdminDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.AdminMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Admin;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AdminRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Service
//@MethodCallLogged //todo!!
@Transactional(propagation = Propagation.REQUIRES_NEW,  readOnly = true, transactionManager = "mokTransactionManager")
public class AdminService {
    @NotNull
    private final AdminRepository adminRepository;

    @NotNull
    private final AccountRepository accountRepository;

    private final AdminMapper adminMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    public Admin createAdmin(Admin newAdmin, Account newAccount) {
        newAccount.setPassword(BCrypt.hashpw(newAccount.getPassword(), BCrypt.gensalt()));
        newAdmin.setAccount(newAccount);
        newAccount.getUserRoles().add(newAdmin);
        //the only difference between this and client is the fact that admin must manually activate the account
        accountRepository.saveAndFlush(newAccount);//todo check if this is correct
        return adminRepository.saveAndFlush(newAdmin);//todo check if this is correct
    }

    public List<AdminDTO> getAdminAccounts() {
        Iterable<Admin> admins = adminRepository.findAll();

        return StreamSupport.stream(admins.spliterator(), false)
                .map(adminMapper::toAdminDTO)
                .collect(Collectors.toList());
    }
}
