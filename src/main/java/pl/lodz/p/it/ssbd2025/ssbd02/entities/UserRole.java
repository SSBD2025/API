package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.Setter;

@Entity
@Table(
        name = "user_role",
        indexes = {
                @Index(name = "ur_user_id_index", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "role"})
        }
)

@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "role")
public abstract class UserRole extends AbstractEntity {

    @Column(name = "role", updatable = false, insertable=false, nullable = true)
    private String roleName;

    @Column(nullable = false)
    @Setter
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @Setter
    private Account account;


    protected UserRole() {}
}
