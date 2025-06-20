package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DietaryRestrictionsConsts;

@Entity
@Table(name = DietaryRestrictionsConsts.TABLE_NAME)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class DietaryRestrictions extends AbstractEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = DietaryRestrictionsConsts.COLUMN_CLIENT_ID, nullable = false, unique = true, updatable = false)
    private Client client;

    @Column(name = DietaryRestrictionsConsts.COLUMN_IS_VEGAN, nullable = false, updatable = true, unique = false)
    private boolean vegan = DietaryRestrictionsConsts.DEFAULT_IS_VEGAN;

    @Column(name = DietaryRestrictionsConsts.COLUMN_IS_VEGETARIAN, nullable = false, updatable = true, unique = false)
    private boolean vegetarian = DietaryRestrictionsConsts.DEFAULT_IS_VEGETARIAN;

    @Column(name = DietaryRestrictionsConsts.COLUMN_IS_KETO, nullable = false, updatable = true, unique = false)
    private boolean keto = DietaryRestrictionsConsts.DEFAULT_IS_KETO;

    @Column(name = DietaryRestrictionsConsts.COLUMN_IS_GLUTEN_FREE, nullable = false, updatable = true, unique = false)
    private boolean glutenFree = DietaryRestrictionsConsts.DEFAULT_IS_GLUTEN_FREE;
}
