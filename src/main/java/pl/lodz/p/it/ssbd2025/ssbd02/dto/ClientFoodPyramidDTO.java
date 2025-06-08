package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;

import java.sql.Timestamp;

@Data
@Setter
@Getter
@AllArgsConstructor
public class ClientFoodPyramidDTO {
    @Valid FoodPyramid foodPyramid;
    boolean isActive;
    Timestamp timestamp;

    public ClientFoodPyramidDTO() {}

    @Override
    public String toString() {
        return "ClientFoodPyramidDTO{" +
                "foodPyramid=" + foodPyramid +
                ", isActive=" + isActive +
                ", timestamp=" + timestamp +
                '}';
    }
}
