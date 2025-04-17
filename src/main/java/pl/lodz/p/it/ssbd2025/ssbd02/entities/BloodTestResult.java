package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.BloodParameter;

@Entity
@Table(name = "blood_test_results",
        indexes = {
                @Index(name = "btr_report_id_index", columnList = "report_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodTestResult extends AbstractEntity {
    @Column(name = "result", nullable = false)
    private String result;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_marker", nullable = false)
    private BloodParameter bloodParameter;

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private ClientBloodTestReport report;
}