package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.BloodParameter;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.BloodTestConsts;

@Entity
@Table(name = BloodTestConsts.TABLE_NAME,
        indexes = {
                @Index(name = BloodTestConsts.REPORT_ID_INDEX, columnList = BloodTestConsts.COLUMN_REPORT_ID)
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class BloodTestResult extends AbstractEntity {
    @Column(name = BloodTestConsts.COLUMN_RESULT, nullable = false)
    @DecimalMin(value = BloodTestConsts.RESULT_MIN)
    private Double result;

    @Enumerated(EnumType.STRING)
    @Column(name = BloodTestConsts.COLUMN_BLOOD_MARKER, nullable = false)
    private BloodParameter bloodParameter;

    @ManyToOne
    @JoinColumn(name = BloodTestConsts.COLUMN_REPORT_ID, nullable = false)
    private ClientBloodTestReport report;
}
