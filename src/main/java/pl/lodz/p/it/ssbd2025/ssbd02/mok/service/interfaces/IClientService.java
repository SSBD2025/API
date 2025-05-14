package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;

import java.util.List;

public interface IClientService {

    Client createClient(Client newClient, Account newAccount);
    List<ClientDTO> getClientAccounts();
    List<AccountDTO> getUnverifiedClientAccounts();
}
