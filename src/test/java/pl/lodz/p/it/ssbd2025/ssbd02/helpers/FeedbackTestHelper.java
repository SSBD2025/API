package pl.lodz.p.it.ssbd2025.ssbd02.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Feedback;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.FeedbackNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.FeedbackRepository;

import java.util.UUID;

@TestComponent
public class FeedbackTestHelper {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
    public Feedback getFeedback(UUID id) {
        return feedbackRepository.findById(id).orElseThrow(FeedbackNotFoundException::new);
    }
}
