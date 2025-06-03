package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = ClientConsts.TABLE_NAME,
        indexes = {
                @Index(name = ClientConsts.DIETICIAN_ID_INDEX, columnList = ClientConsts.COLUMN_DIETICIAN_ID),
        })
@DiscriminatorValue(ClientConsts.DISCRIMINATOR_VALUE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Client extends UserRole {

    @ManyToOne
    @JoinColumn(name = ClientConsts.COLUMN_DIETICIAN_ID, nullable = true)
    private Dietician dietician;

    @OneToMany(mappedBy = PeriodicSurveyConsts.FIELD_CLIENT)
    private List<PeriodicSurvey> periodicSurveys = new ArrayList<>();

    @ToString.Exclude
    @OneToOne(mappedBy = SurveyConsts.FIELD_CLIENT)
    private Survey survey;

    @OneToMany(mappedBy = BloodTestConsts.FIELD_CLIENT)
    private List<ClientBloodTestReport> bloodTestReports = new ArrayList<>();

    @OneToMany(mappedBy = FoodPyramidConsts.FIELD_CLIENT)
    private List<ClientFoodPyramid> foodPyramidIds = new ArrayList<>();

    @OneToOne(mappedBy = DietaryRestrictionsConsts.FIELD_CLIENT, cascade = CascadeType.PERSIST)
    private DietaryRestrictions dietaryRestrictions;

    @OneToMany(mappedBy = FeedbackConsts.FIELD_CLIENT)
    private List<Feedback> feedbacks = new ArrayList<>();
}
