package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.Feedback;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IFeedbackService;

import java.util.List;
import java.util.UUID;

public class FeedbackService implements IFeedbackService {
    @Override
    public List<Feedback> getFeedbacksByClientId(UUID clientId) {
        return List.of();
    }

    @Override
    public List<Feedback> getFeedbacksByFoodPyramidId(UUID pyramidId) {
        return List.of();
    }

    @Override
    public Feedback addFeedback(UUID clientId, UUID pyramidId, Feedback feedback) {
        return null;
    }

    @Override
    public void deleteFeedback(UUID feedbackId) {

    }
}
