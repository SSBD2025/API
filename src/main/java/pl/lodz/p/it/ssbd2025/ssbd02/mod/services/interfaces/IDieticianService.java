package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AssignDietPlanDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodTestOrderDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.PeriodicSurveyDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.*;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface IDieticianService {
    Dietician getById(UUID id);
    List<Client> getClients(UUID dieticianId);
    List<Client> getClientsByDietician(String searchPhrase);
    Survey getPermanentSurveyByClientId(UUID clientId);
    Client getClientDetails(UUID clientId);
    BloodTestOrder orderMedicalExaminations(BloodTestOrderDTO bloodTestOrderDTO);
    List<BloodTestOrder> getUnfulfilledBloodTestOrders();
    Page<PeriodicSurveyDTO> getPeriodicSurveysByAccountId(UUID clientId, Pageable pageable, @Nullable Timestamp since, @Nullable Timestamp before);
    Client getDieticiansClientById(UUID id);
}
