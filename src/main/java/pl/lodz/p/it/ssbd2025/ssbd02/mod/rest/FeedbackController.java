package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FeedbackDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.FeedbackMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Feedback;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IFeedbackService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/feedbacks")
public class FeedbackController {

    private final IFeedbackService feedbackService;

    private final FeedbackMapper feedbackMapper;

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

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/client/{clientId}/pyramid/{pyramidId}")
    public ResponseEntity<FeedbackDTO> addFeedback(@PathVariable UUID clientId, @PathVariable UUID pyramidId, @Validated @RequestBody FeedbackDTO feedbackDTO) {
        Feedback feedback = feedbackService.addFeedback(clientId, pyramidId, feedbackMapper.toFeedback(feedbackDTO));
        FeedbackDTO responseDTO = feedbackMapper.toFeedbackDTO(feedback);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable UUID feedbackId) {
        // Implementation will be added later
        return null;
    }
}