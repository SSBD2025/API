package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.UserRoleConsts;

@Getter
@Entity
@Table(
        name = UserRoleConsts.TABLE_NAME,
        indexes = {
                @Index(name = UserRoleConsts.USER_ID_INDEX, columnList = UserRoleConsts.COLUMN_USER_ID)
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {UserRoleConsts.COLUMN_USER_ID, UserRoleConsts.COLUMN_ROLE})
        }
)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = UserRoleConsts.COLUMN_ROLE)
@ToString(callSuper = true)
public abstract class UserRole extends AbstractEntity {

    @Column(name = UserRoleConsts.COLUMN_ROLE, updatable = false, insertable=false, nullable = true)
    private String roleName;

    @Column(nullable = false)
    @Setter
    private boolean active = UserRoleConsts.DEFAULT_ACTIVE;

    @ManyToOne
    @JoinColumn(name = UserRoleConsts.COLUMN_USER_ID, nullable = false, updatable = false)
    @Setter
    private Account account;


    protected UserRole() {}
}
