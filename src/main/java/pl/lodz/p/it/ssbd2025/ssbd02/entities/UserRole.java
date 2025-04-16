package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.Setter;

@Entity
@Table(name = "user_role",
indexes = {
        @Index(name = "ur_user_id_index", columnList = "user_id")
})
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "role")
public abstract class UserRole extends AbstractEntity {

    //Recznie dodac do bazy w init
    @Column(name = "role_name", updatable = false, insertable=false)
    private String roleName;

    @Column(nullable = false)
    @Setter
    private boolean active = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @Setter
    private Account account;


    protected UserRole() {}
}
