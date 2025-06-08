package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FeedbackDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Feedback;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ClientNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.FoodPyramidNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientModRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.FeedbackRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.FoodPyramidRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IFeedbackService;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@TransactionLogged
@Component
@RequiredArgsConstructor
@Service
@MethodCallLogged
@EnableMethodSecurity(prePostEnabled=true)
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager", timeoutString = "${transaction.timeout}")
public class FeedbackService implements IFeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final ClientModRepository clientRepository;
    private final FoodPyramidRepository foodPyramidRepository;

    @Override
    public List<Feedback> getFeedbacksByClientId(UUID clientId) {
        return List.of();
    }

    @Override
    public List<Feedback> getFeedbacksByFoodPyramidId(UUID pyramidId) {
        return List.of();
    }

    @Override
    @PreAuthorize("hasRole('CLIENT')")
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = false,
            timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    public Feedback addFeedback(UUID clientId, UUID pyramidId, Feedback feedback) {
        feedback.setTimestamp(new Timestamp(System.currentTimeMillis()));

        Client client = clientRepository.findById(clientId).orElseThrow(ClientNotFoundException::new);
        FoodPyramid pyramid = foodPyramidRepository.findById(pyramidId).orElseThrow(FoodPyramidNotFoundException::new);

        feedback.setClient(client);
        feedback.setFoodPyramid(pyramid);
        feedbackRepository.saveAndFlush(feedback);

        Double avg = feedbackRepository.calculateAverageRating(pyramidId);
        pyramid.setAverageRating(avg != null ? avg: 0.0);
        foodPyramidRepository.saveAndFlush(pyramid);

        return feedback;
    }

    @Override
    public void deleteFeedback(UUID feedbackId) {

    }
}

