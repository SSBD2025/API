package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AdminConsts;

@Entity
@Table(name = AdminConsts.TABLE_NAME)
@DiscriminatorValue(AdminConsts.DISCRIMINATOR_VALUE)
@Getter
@Setter
@ToString(callSuper = true)
public class Admin extends UserRole {
    public Admin() {

    }
}
