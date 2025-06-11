package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FeedbackDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.FeedbackMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Feedback;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IFeedbackService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/feedbacks")
public class FeedbackController {

    private final IFeedbackService feedbackService;

    private final FeedbackMapper feedbackMapper;
    private final LockTokenService lockTokenService;


    @PreAuthorize("hasRole('DIETICIAN')")
    @GetMapping("/client/id/{clientId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbacksByClientId(@PathVariable UUID clientId) {
        return ResponseEntity.ok().body(
                feedbackMapper.toFeedbackDTOs(
                        feedbackService.getFeedbacksByClientId(clientId)
                )
        );
    }

    @PreAuthorize("hasRole('DIETICIAN')")
    @GetMapping("/client/login/{login}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbacksByClientLogin(@PathVariable String login) {
        return ResponseEntity.ok().body(
                feedbackMapper.toFeedbackDTOs(
                        feedbackService.getFeedbacksByClientLogin(login)
                )
        );
    }

    @PreAuthorize("hasRole('DIETICIAN')")
    @GetMapping("/pyramid/{pyramidId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbacksByFoodPyramidId(@PathVariable UUID pyramidId) {
        return ResponseEntity.ok().body(
                feedbackMapper.toFeedbackDTOs(
                        feedbackService.getFeedbacksByFoodPyramidId(pyramidId)
                )
        );
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/pyramid/{pyramidId}")
    public ResponseEntity<FeedbackDTO> addFeedback(@PathVariable UUID pyramidId, @Validated @RequestBody FeedbackDTO feedbackDTO) {
        Feedback feedback = feedbackService.addFeedback(pyramidId, feedbackMapper.toFeedback(feedbackDTO));
        FeedbackDTO responseDTO = feedbackMapper.toFeedbackDTO(feedback);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('CLIENT')")
    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable UUID feedbackId) {
        feedbackService.deleteFeedback(feedbackId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PutMapping()
    public ResponseEntity<FeedbackDTO> updateFeedback(@Validated(OnUpdate.class) @RequestBody FeedbackDTO feedbackDTO) {
        Feedback feedback = feedbackService.updateFeedback(feedbackMapper.toFeedback(feedbackDTO), feedbackDTO.getLockToken());
        String lockToken = lockTokenService.generateToken(feedback.getId(), feedback.getVersion()).getValue();
        FeedbackDTO responseDTO = feedbackMapper.toFeedbackDTO(feedback);
        responseDTO.setLockToken(lockToken);
        return ResponseEntity.ok(responseDTO);
    }
}