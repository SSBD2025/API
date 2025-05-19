package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.BloodTestConsts;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = BloodTestConsts.CLIENT_REPORT_TABLE_NAME,
        indexes = {
                @Index(name = BloodTestConsts.CLIENT_ID_INDEX, columnList = BloodTestConsts.COLUMN_CLIENT_ID)
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class ClientBloodTestReport extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = BloodTestConsts.COLUMN_CLIENT_ID, nullable = false, updatable = false)
    private Client client;

    @Column(name = BloodTestConsts.COLUMN_TIMESTAMP, nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp timestamp;

    @OneToMany(mappedBy = "report", cascade = CascadeType.PERSIST)
    private List<BloodTestResult> results;
}
