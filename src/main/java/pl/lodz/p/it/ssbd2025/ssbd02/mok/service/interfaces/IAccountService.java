package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.*;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.AccessRole;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public interface IAccountService {

    void changePassword(ChangePasswordDTO changePasswordDTO);
    void setGeneratedPassword(UUID uuid);
    SensitiveDTO login(String username, SensitiveDTO password, String ipAddress, HttpServletResponse response);
    void logout(HttpServletResponse response);
    void blockAccount(UUID id);
    void unblockAccount(UUID id);
    void sendResetPasswordEmail(String email);
    void resetPassword(SensitiveDTO token, ResetPasswordDTO resetPasswordDTO);
    Page<AccountWithRolesDTO> getAllAccounts(Boolean active, Boolean verified, String searchPhrase, Pageable pageable);
    void changeOwnEmail(String newEmail);
    void changeUserEmail(UUID accountId, String newEmail);
    void confirmEmail(SensitiveDTO token);
    void revertEmailChange(SensitiveDTO token);
    void resendEmailChangeLink();
    AccountWithTokenDTO getAccountByLogin(String login);
    AccountWithTokenDTO getAccountById(UUID id);
    void updateAccountById(UUID id, UpdateAccountDTO dto);
    void updateAccount(Supplier< Account > accountSupplier, UpdateAccountDTO dto);
    void updateMyAccount(String login, UpdateAccountDTO dto);
    void verifyAccount(SensitiveDTO token);
    void assignRole(UUID accountId, AccessRole accessRole, String login);
    void unassignRole(UUID accountId, AccessRole accessRole, String login);
    SensitiveDTO verifyTwoFactorCode(SensitiveDTO code, String ipAddress, HttpServletResponse response);
    void enableTwoFactor();
    void disableTwoFactor();
    void logUserRoleChange(String login, String previousRole, String newRole);
}
