package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FeedbackDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.FeedbackService;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IFeedbackService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/feedbacks")
public class FeedbackController {

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbacksByClientId(@PathVariable UUID clientId) {
        // Implementation will be added later
        return null;
    }

    @GetMapping("/pyramid/{pyramidId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbacksByFoodPyramidId(@PathVariable UUID pyramidId) {
        // Implementation will be added later
        return null;
    }

    @PostMapping("/client/{clientId}/pyramid/{pyramidId}")
    public ResponseEntity<FeedbackDTO> addFeedback(@PathVariable UUID clientId, @PathVariable UUID pyramidId, @RequestBody FeedbackDTO feedback) {
        // Implementation will be added later
        return null;
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable UUID feedbackId) {
        // Implementation will be added later
        return null;
    }
}
