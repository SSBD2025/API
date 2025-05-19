package pl.lodz.p.it.ssbd2025.ssbd02.entities;

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
@ToString(callSuper = true)
public class ClientFoodPyramid {

    @EmbeddedId
    private ClientFoodPyramidId id = new ClientFoodPyramidId();

    @ManyToOne
    @MapsId("clientId")
    @JoinColumn(name = ClientFoodPyramidConsts.COLUMN_CLIENT_ID)
    private Client client;

    @ManyToOne
    @MapsId("foodPyramidId")
    @JoinColumn(name = ClientFoodPyramidConsts.COLUMN_FOOD_PYRAMID_ID)
    private FoodPyramid foodPyramid;

    @Column(name = ClientFoodPyramidConsts.COLUMN_TIMESTAMP, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp timestamp;

    @Version
    @Column(name = ClientFoodPyramidConsts.COLUMN_VERSION, nullable = false)
    private Long version;
}
