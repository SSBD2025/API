package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dietary_restrictions")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DietaryRestrictions extends AbstractEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    @Column(name = "is_vegan", nullable = false)
    private boolean vegan;

    @Column(name = "is_vegetarian", nullable = false)
    private boolean vegetarian;

    @Column(name = "is_keto", nullable = false)
    private boolean keto;

    @Column(name = "is_gluten_free", nullable = false)
    private boolean glutenFree;
}
