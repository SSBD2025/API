package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.FoodPyramidConsts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = FoodPyramidConsts.TABLE_NAME,
        indexes = {@Index(name = FoodPyramidConsts.NAME_INDEX, columnList = FoodPyramidConsts.COLUMN_NAME)}
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class FoodPyramid extends AbstractEntity {

    @OneToMany(mappedBy = FoodPyramidConsts.FIELD_FOOD_PYRAMID)
    @JsonManagedReference
    private List<ClientFoodPyramid> clientFoodPyramids = new ArrayList<>();

    @OneToMany(mappedBy = FoodPyramidConsts.FIELD_FOOD_PYRAMID, cascade = CascadeType.REFRESH)
    private List<Feedback> feedbacks = new ArrayList<>();

    @DecimalMin(FoodPyramidConsts.MIN_DECIMAL)
    @DecimalMax(FoodPyramidConsts.MAX_DECIMAL)
    @Column(name = FoodPyramidConsts.COLUMN_AVERAGE_RATING, nullable = false, updatable = true)
    private double averageRating = FoodPyramidConsts.DEFAULT_AVERAGE_RATING;

    @Size(min = FoodPyramidConsts.MIN_NAME, max = FoodPyramidConsts.MAX_NAME)
    @Column(name = FoodPyramidConsts.COLUMN_NAME, nullable = false, updatable = false, unique = true)
    private String name;

    @Min(FoodPyramidConsts.MIN_VALUE)
    @Column(name = FoodPyramidConsts.COLUMN_KCAL, nullable = false, updatable = false)
    private int kcal;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, inclusive = true)
    @Column(name = FoodPyramidConsts.COLUMN_FAT, nullable = false, updatable = false)
    private double fat;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, inclusive = true)
    @Column(name = FoodPyramidConsts.COLUMN_SFA, nullable = false, updatable = false)
    private double saturatedFattyAcids;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, inclusive = true)
    @Column(name = FoodPyramidConsts.COLUMN_CARBO, nullable = false, updatable = false)
    private double carbohydrates;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, inclusive = true)
    @Column(name = FoodPyramidConsts.COLUMN_SUGAR, nullable = false, updatable = false)
    private double sugar;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, inclusive = true)
    @Column(name = FoodPyramidConsts.COLUMN_PROTEIN, nullable = false, updatable = false)
    private double protein;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, inclusive = true)
    @Column(name = FoodPyramidConsts.COLUMN_A, nullable = false, updatable = false)
    private double A;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, inclusive = true)
    @Column(name = FoodPyramidConsts.COLUMN_D, nullable = false, updatable = false)
    private double D;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, inclusive = true)
    @Column(name = FoodPyramidConsts.COLUMN_E, nullable = false, updatable = false)
    private double E;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, inclusive = true)
    @Column(name = FoodPyramidConsts.COLUMN_K, nullable = false, updatable = false)
    private double K;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, inclusive = true)
    @Column(name = FoodPyramidConsts.COLUMN_B1, nullable = false, updatable = false)
    private double B1;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, inclusive = true)
    @Column(name = FoodPyramidConsts.COLUMN_B2, nullable = false, updatable = false)
    private double B2;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, inclusive = true)
    @Column(name = FoodPyramidConsts.COLUMN_B3, nullable = false, updatable = false)
    private double B3;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, inclusive = true)
    @Column(name = FoodPyramidConsts.COLUMN_B5, nullable = false, updatable = false)
    private double B5;

    @Column(name = FoodPyramidConsts.COLUMN_B6, nullable = false, updatable = false)
    private double B6;

    @Column(name = FoodPyramidConsts.COLUMN_B7, nullable = false, updatable = false)
    private double B7;

    @Column(name = FoodPyramidConsts.COLUMN_B9, nullable = false, updatable = false)
    private double B9;

    @Column(name = FoodPyramidConsts.COLUMN_B12, nullable = false, updatable = false)
    private double B12;

    @Column(name = FoodPyramidConsts.COLUMN_C, nullable = false, updatable = false)
    private double C;

    @Column(name = FoodPyramidConsts.COLUMN_POTASSIUM, nullable = false, updatable = false)
    private double Potassium;

    @Column(name = FoodPyramidConsts.COLUMN_CALCIUM, nullable = false, updatable = false)
    private double Calcium;

    @Column(name = FoodPyramidConsts.COLUMN_PHOSPHORUS, nullable = false, updatable = false)
    private double Phosphorus;

    @Column(name = FoodPyramidConsts.COLUMN_MAGNESIUM, nullable = false, updatable = false)
    private double Magnesium;

    @Column(name = FoodPyramidConsts.COLUMN_IRON, nullable = false, updatable = false)
    private double Iron;

    @Column(name = FoodPyramidConsts.COLUMN_ZINC, nullable = false, updatable = false)
    private double Zinc;

    @Column(name = FoodPyramidConsts.COLUMN_FLUORINE, nullable = false, updatable = false)
    private double Fluorine;

    @Column(name = FoodPyramidConsts.COLUMN_MANGANESE, nullable = false, updatable = false)
    private double Manganese;

    @Column(name = FoodPyramidConsts.COLUMN_COPPER, nullable = false, updatable = false)
    private double Copper;

    @Column(name = FoodPyramidConsts.COLUMN_IODINE, nullable = false, updatable = false)
    private double Iodine;

    @Column(name = FoodPyramidConsts.COLUMN_SELENIUM, nullable = false, updatable = false)
    private double Selenium;

    @Column(name = FoodPyramidConsts.COLUMN_MOLYBDENUM, nullable = false, updatable = false)
    private double Molybdenum;

    @Column(name = FoodPyramidConsts.COLUMN_CHROMIUM, nullable = false, updatable = false)
    private double Chromium;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodPyramid that = (FoodPyramid) o;
        return kcal == that.kcal && Double.compare(fat, that.fat) == 0 && Double.compare(saturatedFattyAcids, that.saturatedFattyAcids) == 0 && Double.compare(carbohydrates, that.carbohydrates) == 0 && Double.compare(sugar, that.sugar) == 0 && Double.compare(protein, that.protein) == 0 && Double.compare(A, that.A) == 0 && Double.compare(D, that.D) == 0 && Double.compare(E, that.E) == 0 && Double.compare(K, that.K) == 0 && Double.compare(B1, that.B1) == 0 && Double.compare(B2, that.B2) == 0 && Double.compare(B3, that.B3) == 0 && Double.compare(B5, that.B5) == 0 && Double.compare(B6, that.B6) == 0 && Double.compare(B7, that.B7) == 0 && Double.compare(B9, that.B9) == 0 && Double.compare(B12, that.B12) == 0 && Double.compare(C, that.C) == 0 && Double.compare(Potassium, that.Potassium) == 0 && Double.compare(Calcium, that.Calcium) == 0 && Double.compare(Phosphorus, that.Phosphorus) == 0 && Double.compare(Magnesium, that.Magnesium) == 0 && Double.compare(Iron, that.Iron) == 0 && Double.compare(Zinc, that.Zinc) == 0 && Double.compare(Fluorine, that.Fluorine) == 0 && Double.compare(Manganese, that.Manganese) == 0 && Double.compare(Copper, that.Copper) == 0 && Double.compare(Iodine, that.Iodine) == 0 && Double.compare(Selenium, that.Selenium) == 0 && Double.compare(Molybdenum, that.Molybdenum) == 0 && Double.compare(Chromium, that.Chromium) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kcal, fat, saturatedFattyAcids, carbohydrates, sugar, protein, A, D, E, K, B1, B2, B3, B5, B6, B7, B9, B12, C, Potassium, Calcium, Phosphorus, Magnesium, Iron, Zinc, Fluorine, Manganese, Copper, Iodine, Selenium, Molybdenum, Chromium);
    }
}
