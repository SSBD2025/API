package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Feedback;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.*;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientFoodPyramidRepository;
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
    private final ClientFoodPyramidRepository  clientFoodPyramidRepository;

    @Override
    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = true,
            timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    public List<Feedback> getFeedbacksByClientId(UUID clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(ClientNotFoundException::new);
        return feedbackRepository.findAllByClient(client);
    }

    @Override
    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = true,
            timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    public List<Feedback> getFeedbacksByClientLogin(String login) {
        Client client = clientRepository.findByLogin(login)
                .orElseThrow(ClientNotFoundException::new);
        return feedbackRepository.findAllByClient(client);
    }

    @Override
    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            readOnly = true,
            timeoutString = "${transaction.timeout}")
    @Retryable(
            retryFor = {JpaSystemException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}")
    public List<Feedback> getFeedbacksByFoodPyramidId(UUID pyramidId) {
        FoodPyramid pyramid = foodPyramidRepository.findById(pyramidId)
                .orElseThrow(FoodPyramidNotFoundException::new);
        return feedbackRepository.findAllByFoodPyramid(pyramid);
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

        if (!clientFoodPyramidRepository.existsByClientAndFoodPyramid(client, pyramid)) {
            throw new NotYourFoodPyramidException();
        }

        if (feedbackRepository.existsByClientIdAndFoodPyramidId(clientId, pyramidId)) {
            throw new AlreadyRatedPyramidException();
        }

        feedback.setClient(client);
        feedback.setFoodPyramid(pyramid);
        feedbackRepository.saveAndFlush(feedback);

        Double avg = feedbackRepository.calculateAverageRating(pyramidId);
        pyramid.setAverageRating(avg != null ? avg: 0.0);
        foodPyramidRepository.saveAndFlush(pyramid);

        return feedback;
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
    public void deleteFeedback(UUID feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId).orElseThrow(FeedbackNotFoundException::new);

        String login = SecurityContextHolder.getContext().getAuthentication().getName();

        Client client = clientRepository.findByLogin(login).orElseThrow(ClientNotFoundException::new);

        if (!feedback.getClient().getId().equals(client.getId())) {
            throw new NotYourFeedbackException();
        }

        FoodPyramid pyramid = feedback.getFoodPyramid();

        feedbackRepository.delete(feedback);

        Double avg = feedbackRepository.calculateAverageRating(pyramid.getId());
        pyramid.setAverageRating(avg != null ? avg: 0.0);
        foodPyramidRepository.saveAndFlush(pyramid);
    }
}

