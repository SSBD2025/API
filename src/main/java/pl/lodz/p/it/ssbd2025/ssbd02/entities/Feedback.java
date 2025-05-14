package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Table(indexes = {
    @Index(name = "feedback_client_id_index", columnList = "client_id"),
    @Index(name = "feedback_food_pyramid_id_index", columnList = "food_pyramid_id")
})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class Feedback extends AbstractEntity {
    @Size(min = 1, max = 5)
    private int rating;
    @Length(min = 1, max = 255)
    private String description;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp timestamp;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false, updatable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "food_pyramid_id", nullable = false, updatable = false)
    private FoodPyramid foodPyramid;
}
