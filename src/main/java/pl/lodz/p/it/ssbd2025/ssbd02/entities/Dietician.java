package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.BloodTestOrderConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ClientConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DieticianConsts;

import java.sql.Timestamp;
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
    @Size(min = 0, max = 10)
    private List<Client> clients = new ArrayList<>();

    @OneToMany(mappedBy = BloodTestOrderConsts.FIELD_DIETICIAN)
    private List<BloodTestOrder> bloodTestOrders = new ArrayList<>();

    @Column(name = DieticianConsts.LAST_ASSIGNED_CLIENT, nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp lastAssignedClient;
}
