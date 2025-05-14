package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "client_blood_test_reports",
    indexes = {
        @Index(name = "cbtr_client_id_index", columnList = "client_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class ClientBloodTestReport extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false, updatable = false)
    private Client client;

    @Column(name = "timestamp", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp timestamp;

    @OneToMany(mappedBy = "report", cascade = CascadeType.PERSIST)
    private List<BloodTestResult> results;
}