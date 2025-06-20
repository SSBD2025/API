package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.FeedbackConsts;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDTO {
    @NotNull(groups = {OnCreate.class})
    @Null(groups = OnCreate.class)
    private UUID id;

    @NotNull(groups = {OnCreate.class, OnUpdate.class, OnRead.class})
    @Min(value = FeedbackConsts.RATING_MIN)
    @Max(value = FeedbackConsts.RATING_MAX)
    private int rating;

    @NotNull(groups = {OnCreate.class, OnUpdate.class, OnRead.class})
    @Size(min = FeedbackConsts.DESCRIPTION_MIN, max = FeedbackConsts.DESCRIPTION_MAX)
    private String description;

    @Null(groups = {OnCreate.class})
    @NotNull(groups = {OnRead.class})
    private Timestamp timestamp;

    @NotNull(groups = {OnRead.class})
    private UUID clientId;

    @NotNull(groups = {OnRead.class})
    private UUID foodPyramidId;

    @NotNull(groups = {OnUpdate.class, OnRead.class})
    @NotBlank(groups = {OnUpdate.class, OnRead.class})
    private String lockToken;
}
