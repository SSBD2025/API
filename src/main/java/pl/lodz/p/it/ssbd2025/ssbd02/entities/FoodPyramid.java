package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.FoodPyramidConsts;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = FoodPyramidConsts.TABLE_NAME)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class FoodPyramid extends AbstractEntity {

    @OneToMany(mappedBy = FoodPyramidConsts.FIELD_FOOD_PYRAMID)
    private List<ClientFoodPyramid> clientFoodPyramids = new ArrayList<>();

    @OneToMany(mappedBy = FoodPyramidConsts.FIELD_FOOD_PYRAMID, cascade = CascadeType.REFRESH)
    private List<Feedback> feedbacks = new ArrayList<>();

    @Column(name = FoodPyramidConsts.COLUMN_AVERAGE_RATING, nullable = false, updatable = true)
    private double averageRating = FoodPyramidConsts.DEFAULT_AVERAGE_RATING;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_A, nullable = false, updatable = false)
    private double A;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_D, nullable = false, updatable = false)
    private double D;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_E, nullable = false, updatable = false)
    private double E;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_K, nullable = false, updatable = false)
    private double K;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_B1, nullable = false, updatable = false)
    private double B1;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_B2, nullable = false, updatable = false)
    private double B2;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_B3, nullable = false, updatable = false)
    private double B3;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_B5, nullable = false, updatable = false)
    private double B5;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_B6, nullable = false, updatable = false)
    private double B6;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_B7, nullable = false, updatable = false)
    private double B7;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_B9, nullable = false, updatable = false)
    private double B9;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_B12, nullable = false, updatable = false)
    private double B12;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_C, nullable = false, updatable = false)
    private double C;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_POTASSIUM, nullable = false, updatable = false)
    private double Potassium;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_CALCIUM, nullable = false, updatable = false)
    private double Calcium;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_PHOSPHORUS, nullable = false, updatable = false)
    private double Phosphorus;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_MAGNESIUM, nullable = false, updatable = false)
    private double Magnesium;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_IRON, nullable = false, updatable = false)
    private double Iron;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_ZINC, nullable = false, updatable = false)
    private double Zinc;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_FLUORINE, nullable = false, updatable = false)
    private double Fluorine;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_MANGANESE, nullable = false, updatable = false)
    private double Manganese;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_COPPER, nullable = false, updatable = false)
    private double Copper;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_IODINE, nullable = false, updatable = false)
    private double Iodine;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_SELENIUM, nullable = false, updatable = false)
    private double Selenium;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_MOLYBDENUM, nullable = false, updatable = false)
    private double Molybdenum;

    @Min(0)
    @Column(name = FoodPyramidConsts.COLUMN_CHROMIUM, nullable = false, updatable = false)
    private double Chromium;
}
