package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodPyramidDetailsDTO {
    @NotNull()
    private FoodPyramidDTO foodPyramid;

    @NotNull()
    private List<FeedbackDTO> feedbacks;

    @NotNull()
    private List<MinimalClientDTO> clients;
}
