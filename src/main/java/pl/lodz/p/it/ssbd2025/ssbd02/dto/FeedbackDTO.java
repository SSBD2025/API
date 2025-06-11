package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDTO {
    private UUID id;
    private int rating;
    private String description;
    private Timestamp timestamp;
    private UUID clientId;
    private UUID foodPyramidId;
    private String lockToken;
}
