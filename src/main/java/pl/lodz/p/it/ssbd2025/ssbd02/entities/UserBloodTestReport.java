package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "user_blood_test_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBloodTestReport extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "timestamp", nullable = false)
    private Timestamp timestamp;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BloodTestResult> results;
}