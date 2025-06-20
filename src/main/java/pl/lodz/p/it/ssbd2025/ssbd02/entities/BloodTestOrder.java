package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.BloodParameter;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.BloodTestOrderConsts;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = BloodTestOrderConsts.TABLE_NAME,
        indexes = {
                @Index(name = BloodTestOrderConsts.CLIENT_ID_INDEX, columnList = BloodTestOrderConsts.COLUMN_CLIENT_ID),
                @Index(name = BloodTestOrderConsts.DIETITIAN_ID_INDEX, columnList = BloodTestOrderConsts.COLUMN_DIETITIAN_ID)
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = {"client", "dietician"})
public class BloodTestOrder extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = BloodTestOrderConsts.COLUMN_CLIENT_ID, nullable = false, updatable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = BloodTestOrderConsts.COLUMN_DIETITIAN_ID, nullable = false, updatable = false)
    private Dietician dietician;

    @Column(name = BloodTestOrderConsts.COLUMN_ORDER_DATE, nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp orderDate;

    @Column(name = BloodTestOrderConsts.COLUMN_DESCRIPTION, nullable = false, columnDefinition = "TEXT")
    @Size(min = BloodTestOrderConsts.DESCRIPTION_MIN, max = BloodTestOrderConsts.DESCRIPTION_MAX)
    private String description;

    @ElementCollection
    @CollectionTable(
            name = BloodTestOrderConsts.PARAMETERS_TABLE_NAME,
            joinColumns = @JoinColumn(name = BloodTestOrderConsts.COLUMN_ORDER_ID)
    )
    @Column(name = BloodTestOrderConsts.COLUMN_PARAMETER, nullable = false)
    @Enumerated(EnumType.STRING)
    @Size(min = BloodTestOrderConsts.PARAMETERS_MIN, max = BloodTestOrderConsts.PARAMETERS_MAX)
    private List<BloodParameter> parameters;

    @Column(name = BloodTestOrderConsts.COLUMN_FULFILLED)
    private boolean fulfilled;
}
