package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.AdminDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Admin;

import java.util.List;

public interface IAdminService {

    Admin createAdmin(Admin newAdmin, Account newAccount);
}
