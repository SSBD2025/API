package pl.lodz.p.it.ssbd2025.ssbd02.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.PermanentSurveyNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.SurveyRepository;

import java.util.UUID;

@TestComponent
public class SurveyHelper {

    @Autowired
    private SurveyRepository surveyRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
    public Survey getSurveyByClientId(UUID clientId) {
        return surveyRepository.findByClientId(clientId).orElseThrow(PermanentSurveyNotFoundException::new);
    }

}
