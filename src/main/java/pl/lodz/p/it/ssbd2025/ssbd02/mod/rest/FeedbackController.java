package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FeedbackDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.FeedbackMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Feedback;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.AuthorizedEndpoint;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IFeedbackService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/feedbacks")
public class FeedbackController {

    private final IFeedbackService feedbackService;

    private final FeedbackMapper feedbackMapper;
    private final LockTokenService lockTokenService;

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/my-pyramid/{pyramidId}")
    @Operation(summary = "Pobierz swoją opinię dla wybranej piramidy",
            description = "Dostępne dla CLIENT.")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opinia została zwrócona pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono piramidy lub opinii dla zalogowanego użytkownika"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowy format identyfikatora (UUID)")
    })
    public ResponseEntity<FeedbackDTO> getMyFeedbackForPyramid(
            @PathVariable UUID pyramidId
    ) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Feedback feedback = feedbackService.getFeedbackByClientLoginAndPyramid(login, pyramidId);
        String lockToken = lockTokenService.generateToken(feedback.getId(), feedback.getVersion()).getValue();
        FeedbackDTO feedbackDTO = feedbackMapper.toFeedbackDTO(feedback);
        feedbackDTO.setLockToken(lockToken);
        return ResponseEntity.ok().body(feedbackDTO);
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/pyramid/{pyramidId}")
    @Operation(summary = "Dodaj opinię do piramidy po id",
            description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Opinia została dodana"),
            @ApiResponse(responseCode = "400", description = "Niepoprawne żądanie"),
            @ApiResponse(responseCode = "403", description = "Ta piramida nie jest przypisana do tego użytkownika"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta o podanym loginie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono piramidy o podanym id"),
            @ApiResponse(responseCode = "409", description = "Ten klient już ocenił tę piramidę")
    })
    public ResponseEntity<FeedbackDTO> addFeedback(@PathVariable UUID pyramidId, @Validated @RequestBody FeedbackDTO feedbackDTO) {
        Feedback feedback = feedbackService.addFeedback(pyramidId, feedbackMapper.toFeedback(feedbackDTO));
        FeedbackDTO responseDTO = feedbackMapper.toFeedbackDTO(feedback);

        URI location = URI.create(String.format("/pyramids/%s/feedback/%s", pyramidId, feedback.getId()));

        return ResponseEntity.created(location).body(responseDTO);
    }

    @PreAuthorize("hasRole('CLIENT')")
    @DeleteMapping("/{feedbackId}")
    @Operation(summary = "Usunięcie opinii piramidy po id",
            description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Opinia została usunięta"),
            @ApiResponse(responseCode = "400", description = "Niepoprawne żądanie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono opinii o podanym id"),
            @ApiResponse(responseCode = "403", description = "Ta piramida nie jest przypisana do tego użytkownika"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta o podanym loginie"),
    })
    public ResponseEntity<Void> deleteFeedback(@PathVariable UUID feedbackId) {
        feedbackService.deleteFeedback(feedbackId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PutMapping()
    @Operation(summary = "Edycja opinii piramidy o podanym id",
            description = "Dostępne dla CLIENT")
    @AuthorizedEndpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opinia została pomyślnie zaktualizowana"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono opinii o podanym id"),
            @ApiResponse(responseCode = "409", description = "Jednoczesna edycja zasobu"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta o podanym loginie"),
            @ApiResponse(responseCode = "403", description = "Klient nie jest autorem edytowanej opinii"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono piramidy o podanym id"),
    })
    public ResponseEntity<FeedbackDTO> updateFeedback(@Validated(OnUpdate.class) @RequestBody FeedbackDTO feedbackDTO) {
        Feedback feedback = feedbackService.updateFeedback(feedbackMapper.toFeedback(feedbackDTO), feedbackDTO.getLockToken());
        FeedbackDTO responseDTO = feedbackMapper.toFeedbackDTO(feedback);
        return ResponseEntity.ok(responseDTO);
    }
}