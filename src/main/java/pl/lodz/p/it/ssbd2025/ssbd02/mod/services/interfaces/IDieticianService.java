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
    List<Client> getClientsByDietician(String searchPhrase);
    Survey getPermanentSurveyByClientId(UUID clientId);
    Client getClientDetails(UUID clientId);
    BloodTestOrder orderMedicalExaminations(BloodTestOrderDTO bloodTestOrderDTO);
    List<BloodTestOrder> getUnfulfilledBloodTestOrders();
    Page<PeriodicSurveyDTO> getPeriodicSurveysByClientId(UUID clientId, Pageable pageable, @Nullable Timestamp since, @Nullable Timestamp before);
    Client getDieticiansClientById(UUID id);
    BloodTestOrderDTO getLastOrder(UUID clientId);
    void confirmBloodTestOrder(UUID orderId);
}
