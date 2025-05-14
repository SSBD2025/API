package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "client_food_pyramid",
    indexes = {
        @Index(name = "cfp_client_id_index", columnList = "client_id"),
        @Index(name = "cfp_food_pyramid_id_index", columnList = "food_pyramid_id"),
        @Index(name = "cfp_timestamp_index", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"client", "foodPyramid"})
@ToString(callSuper = true)
public class ClientFoodPyramid {

    @EmbeddedId
    private ClientFoodPyramidId id = new ClientFoodPyramidId();

    @ManyToOne
    @MapsId("clientId")
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @MapsId("foodPyramidId")
    @JoinColumn(name = "food_pyramid_id")
    private FoodPyramid foodPyramid;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp timestamp;

    @Version
    @Column(nullable = false)
    private Long version;
}
