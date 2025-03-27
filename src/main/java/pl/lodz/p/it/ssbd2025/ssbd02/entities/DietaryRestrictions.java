package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "dietary_restrictions")
@Data
public class DietaryRestrictions {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "is_vegan", nullable = false)
    private boolean vegan;

    @Column(name = "is_vegetarian", nullable = false)
    private boolean vegetarian;

    @Column(name = "is_keto", nullable = false)
    private boolean keto;

    @Column(name = "is_gluten_free", nullable = false)
    private boolean glutenFree;
}
