package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.DieticianDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;

import java.util.List;

public interface IDieticianService {

    Dietician createDietician(Dietician newDietician, Account newAccount);
    List<DieticianDTO> getDieticianAccounts();
    List<AccountDTO> getUnverifiedDieticianAccounts();
}
