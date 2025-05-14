package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class ClientFoodPyramidId implements Serializable {

    private UUID clientId;
    private UUID foodPyramidId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientFoodPyramidId that)) return false;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(foodPyramidId, that.foodPyramidId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, foodPyramidId);
    }
}
