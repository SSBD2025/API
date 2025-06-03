package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;

import java.util.List;
import java.util.UUID;

public interface IClientService {
    Client getClientById(UUID id);
    List<Client> getClientsByDieticianId(UUID dieticianId);
    void assignDietician(UUID clientId, UUID dieticianId);
    Survey submitPermanentSurvey(Survey newSurvey);
}
