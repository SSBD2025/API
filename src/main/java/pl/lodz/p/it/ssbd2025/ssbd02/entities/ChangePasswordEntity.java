package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ChangePasswordConsts;

@Entity
@Table(name = ChangePasswordConsts.CHANGE_PASSWORD_TABLE_NAME)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(callSuper = true)
public class ChangePasswordEntity extends AbstractEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = ChangePasswordConsts.ACCOUNT_COLUMN_NAME)
    @NotNull
    private Account account;

    @Setter
    @Column(name = ChangePasswordConsts.PASSWORD_COLUMN_NAME, nullable = false, updatable = true)
    private boolean passwordToChange = true;
}
