package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.Feedback;

import java.util.List;
import java.util.UUID;

public interface IFeedbackService {
    Feedback addFeedback(UUID pyramidId, Feedback feedback);
    void deleteFeedback(UUID feedbackId);
    Feedback updateFeedback(Feedback feedback, String lockToken);
    Feedback getFeedbackByClientLoginAndPyramid(String login, UUID pyramidId);
}
