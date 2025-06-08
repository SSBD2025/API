package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodPyramidDetailsDTO {
    private FoodPyramidDTO foodPyramid;
    private List<FeedbackDTO> feedbacks;
    private List<MinimalClientDTO> clients;
}
