package pl.lodz.p.it.ssbd2025.ssbd02.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.PeriodicSurvey;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.PeriodicSurveyNotFound;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.PeriodicSurveyRepository;

import java.util.UUID;

@TestComponent
public class PeriodicSurveyHelper {

    @Autowired
    private PeriodicSurveyRepository periodicSurveyRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
    public PeriodicSurvey getSurveyById(UUID surveyId) {
        return periodicSurveyRepository.findById(surveyId).orElseThrow(PeriodicSurveyNotFound::new);
    }
}
