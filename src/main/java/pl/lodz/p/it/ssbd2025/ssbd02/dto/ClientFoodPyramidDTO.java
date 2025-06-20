package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;

import java.sql.Timestamp;

@Data
@Setter
@Getter
@AllArgsConstructor
public class ClientFoodPyramidDTO {
    @Valid FoodPyramidDTO foodPyramid;
    boolean isActive;
    @NotNull(groups = {OnCreate.class, OnUpdate.class, OnRead.class})
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
