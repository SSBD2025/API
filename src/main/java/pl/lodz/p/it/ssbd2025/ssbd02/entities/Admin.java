package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "admin")
@DiscriminatorValue("ADMIN")
@Getter
@Setter
@ToString(callSuper = true)
public class Admin extends UserRole {
    public Admin() {

    }
}
