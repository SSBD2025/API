package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import jakarta.persistence.PreUpdate;
import org.springframework.beans.factory.annotation.Value;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.PeriodicSurvey;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.LateUpdateException;

import java.time.Duration;
import java.time.Instant;

public class PersistenceListeners {

    @Value("${app.entity.periodic_survey.updatable_for}")
    private int periodicSurveyUpdatableFor;

    @PreUpdate
    public void preventLateUpdate(PeriodicSurvey periodicSurvey) {
        if(Duration.between(periodicSurvey.getCreatedAt().toInstant(), Instant.now()).toSeconds() > periodicSurveyUpdatableFor) {
            throw new LateUpdateException();
        }
    }
}