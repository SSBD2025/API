package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ClientConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DieticianConsts;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = DieticianConsts.TABLE_NAME)
@DiscriminatorValue(DieticianConsts.DISCRIMINATOR_VALUE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Dietician extends UserRole {

    @OneToMany(mappedBy = ClientConsts.FIELD_DIETICIAN, cascade = CascadeType.REFRESH)
    @ToString.Exclude
    private List<Client> clients = new ArrayList<>();
}
