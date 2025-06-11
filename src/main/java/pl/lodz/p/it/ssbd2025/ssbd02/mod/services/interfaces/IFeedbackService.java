package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.Feedback;

import java.util.List;
import java.util.UUID;

public interface IFeedbackService {
    List<Feedback> getFeedbacksByClientId(UUID clientId);
    List<Feedback> getFeedbacksByFoodPyramidId(UUID pyramidId);
    Feedback addFeedback(UUID pyramidId, Feedback feedback);
    void deleteFeedback(UUID feedbackId);
    List<Feedback> getFeedbacksByClientLogin(String login);
}
