package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.PeriodicSurveyDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SurveyDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.PeriodicSurvey;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface IClientService {
    Client getClientById(UUID id);
    UUID getClientByAccountId(UUID id);
    Client getClientByLogin(SensitiveDTO login);
    void assignDietician(UUID dieticianId);
    Survey submitPermanentSurvey(Survey newSurvey);
    public Survey getPermanentSurvey();
    List<Dietician> getAvailableDieticians(String searchPhrase);
    public PeriodicSurvey submitPeriodicSurvey(PeriodicSurvey periodicSurvey);
    Survey editPermanentSurvey(SurveyDTO dto);
    Page<PeriodicSurveyDTO> getPeriodicSurveys(UUID clientId, Pageable pageable);
    Page<PeriodicSurveyDTO> getPeriodicSurveys(Pageable pageable, @Nullable Timestamp since, @Nullable Timestamp before);
    PeriodicSurveyDTO getPeriodicSurvey(UUID periodicSurveyId);
    PeriodicSurveyDTO editPeriodicSurvey(PeriodicSurveyDTO dto);
    PeriodicSurveyDTO getMyLatestPeriodicSurvey();
}
