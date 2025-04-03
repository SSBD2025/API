package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@MappedSuperclass
@Getter
public abstract class AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Version
    @Column(nullable = false)
    private Long version;
}
