package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "blood_test_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "blood_marker_id", nullable = false)
    private UUID bloodMarkerId;

    @Column(name = "result", nullable = false)
    private String result;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false)
    private Unit unit;

    @Column(name = "report_id", nullable = false)
    private UUID reportId;

    @ManyToOne
    @JoinColumn(name = "blood_marker_id", insertable = false, updatable = false)
    private BloodMarker bloodMarker;
}