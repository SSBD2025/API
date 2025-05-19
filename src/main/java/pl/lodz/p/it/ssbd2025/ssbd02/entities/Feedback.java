package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.FeedbackConsts;

@Table(name = FeedbackConsts.TABLE_NAME,
        indexes = {
                @Index(name = FeedbackConsts.CLIENT_ID_INDEX, columnList = FeedbackConsts.COLUMN_CLIENT_ID),
                @Index(name = FeedbackConsts.FOOD_PYRAMID_ID_INDEX, columnList = FeedbackConsts.COLUMN_FOOD_PYRAMID_ID)
        })
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class Feedback extends AbstractEntity {
    @Size(min = FeedbackConsts.RATING_MIN, max = FeedbackConsts.RATING_MAX)
    @Column(name = FeedbackConsts.COLUMN_RATING)
    private int rating;

    @Length(min = FeedbackConsts.DESCRIPTION_MIN, max = FeedbackConsts.DESCRIPTION_MAX)
    @Column(name = FeedbackConsts.COLUMN_DESCRIPTION)
    private String description;

    @Column(name = FeedbackConsts.COLUMN_TIMESTAMP)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp timestamp;

    @ManyToOne
    @JoinColumn(name = FeedbackConsts.COLUMN_CLIENT_ID, nullable = false, updatable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = FeedbackConsts.COLUMN_FOOD_PYRAMID_ID, nullable = false, updatable = false)
    private FoodPyramid foodPyramid;
}
