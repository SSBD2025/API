package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class ClientStatusDTO {
    @NotNull
    private Boolean hasAssignedDietician;
    @NotNull
    private Boolean hasSubmittedPermanentSurvey;

    public ClientStatusDTO() {}

    @Override
    public String toString() {
        return "ClientStatusDTO{" +
                "hasAssignedDietician=" + hasAssignedDietician +
                ", hasSubmittedPermanentSurvey=" + hasSubmittedPermanentSurvey +
                '}';
    }
}
