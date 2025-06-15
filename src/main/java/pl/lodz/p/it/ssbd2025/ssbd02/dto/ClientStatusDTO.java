package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class ClientStatusDTO {
    private boolean hasAssignedDietician;

    public ClientStatusDTO() {}

    @Override
    public String toString() {
        return "ClientStatusDTO{" +
                "hasAssignedDietician=" + hasAssignedDietician +
                '}';
    }
}
