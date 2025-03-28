package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "blood_test_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "result", nullable = false)
    private String result;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false)
    private Unit unit;

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private UserBloodTestReport report;

    @ManyToOne
    @JoinColumn(name = "blood_marker_id", nullable = false)
    private BloodMarker bloodMarker;
}