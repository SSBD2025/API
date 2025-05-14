package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.*;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.AccessRole;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public interface IAccountService {

    void changePassword(ChangePasswordDTO changePasswordDTO);
    String setGeneratedPassword(UUID uuid);
    TokenPairDTO login(String username, String password, String ipAddress);
    void logout();
    void blockAccount(UUID id);
    void unblockAccount(UUID id);
    void sendResetPasswordEmail(String email);
    void resetPassword(String token, ResetPasswordDTO resetPasswordDTO);
    List<AccountWithRolesDTO> getAllAccounts(Boolean active, Boolean verified);
    void changeOwnEmail(String newEmail);
    void changeUserEmail(UUID accountId, String newEmail);
    void confirmEmail(String token);
    void revertEmailChange(String token);
    void resendEmailChangeLink();
    AccountWithTokenDTO getAccountByLogin(String login);
    AccountWithTokenDTO getAccountById(String id);
    void updateAccountById(String id, UpdateAccountDTO dto);
    void updateAccount(Supplier< Account > accountSupplier, UpdateAccountDTO dto);
    void updateMyAccount(String login, UpdateAccountDTO dto);
    void verifyAccount(String token);
    void assignRole(UUID accountId, AccessRole accessRole, String login);
    void unassignRole(UUID accountId, AccessRole accessRole, String login);

}
