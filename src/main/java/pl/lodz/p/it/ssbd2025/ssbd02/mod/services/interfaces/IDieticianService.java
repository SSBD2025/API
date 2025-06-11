package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.AssignDietPlanDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;

import java.util.List;
import java.util.UUID;

public interface IDieticianService {
    Dietician getById(UUID id);
    List<Client> getClients(UUID dieticianId);
    List<Client> getClientsByDietician(String searchPhrase);
    Survey getPermanentSurveyByClientId(UUID clientId);
    Client getClientDetails(UUID clientId);
}
