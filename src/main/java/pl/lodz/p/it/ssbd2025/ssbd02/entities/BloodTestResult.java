package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "blood_test_results")
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