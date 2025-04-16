package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dietician")
@DiscriminatorValue("DIETICIAN")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Dietician extends UserRole {

    @OneToMany(mappedBy = "dietician", cascade = CascadeType.REFRESH)
    private List<Client> clients = new ArrayList<>();
}
