package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ClientFoodPyramidConsts;

import java.sql.Timestamp;

@Entity
@Table(name = ClientFoodPyramidConsts.TABLE_NAME,
        indexes = {
                @Index(name = ClientFoodPyramidConsts.CLIENT_ID_INDEX, columnList = ClientFoodPyramidConsts.COLUMN_CLIENT_ID),
                @Index(name = ClientFoodPyramidConsts.FOOD_PYRAMID_ID_INDEX, columnList = ClientFoodPyramidConsts.COLUMN_FOOD_PYRAMID_ID),
                @Index(name = ClientFoodPyramidConsts.TIMESTAMP_INDEX, columnList = ClientFoodPyramidConsts.COLUMN_TIMESTAMP)
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"client", "foodPyramid"})
@ToString(callSuper = true, exclude = {"client", "foodPyramid"})
public class ClientFoodPyramid {

    @EmbeddedId
    private ClientFoodPyramidId id = new ClientFoodPyramidId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("clientId")
    @JsonBackReference
    @JoinColumn(name = ClientFoodPyramidConsts.COLUMN_CLIENT_ID, nullable = false , updatable = false, unique = false)
    private Client client;

    @ManyToOne
    @MapsId("foodPyramidId")
    @JoinColumn(name = ClientFoodPyramidConsts.COLUMN_FOOD_PYRAMID_ID, nullable = false, updatable = false, unique = false)
    @JsonBackReference
    private FoodPyramid foodPyramid;

    @Column(name = ClientFoodPyramidConsts.COLUMN_TIMESTAMP, nullable = false, updatable = false, unique = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp timestamp;

    @Version
    @Column(name = ClientFoodPyramidConsts.COLUMN_VERSION, nullable = false, updatable = true, unique = false)
    private Long version;

    public ClientFoodPyramid(Client client, FoodPyramid foodPyramid, Timestamp timestamp) {
        this.client = client;
        this.foodPyramid = foodPyramid;
        this.timestamp = timestamp;
        this.id = new ClientFoodPyramidId(client.getId(), foodPyramid.getId());
    }
}
