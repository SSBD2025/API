package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.PeriodicSurvey;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;

import java.util.List;
import java.util.UUID;

public interface IClientService {
    Client getClientById(UUID id);
    UUID getClientByAccountId(UUID id);
    Client getClientByLogin(SensitiveDTO login);
    void assignDietician(UUID clientId, UUID dieticianId);
    Survey submitPermanentSurvey(Survey newSurvey);
    List<Dietician> getAvailableDieticians(String searchPhrase);
    public PeriodicSurvey submitPeriodicSurvey(PeriodicSurvey periodicSurvey);
}
