package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.FoodPyramidConsts;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodPyramidDTO {
    @Null(groups = OnCreate.class)
    @NotNull(groups = OnUpdate.class)
    private UUID id;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL)
    @DecimalMax(value = FoodPyramidConsts.MAX_DECIMAL)
    private double averageRating;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Size(min = FoodPyramidConsts.MIN_NAME, max = FoodPyramidConsts.MAX_NAME, groups = {OnCreate.class, OnUpdate.class})
    private String name;

    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private int kcal;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double fat;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double saturatedFattyAcids;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double carbohydrates;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double sugar;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double protein;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double A;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double D;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double E;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double K;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double B1;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double B2;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double B3;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double B5;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double B6;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double B7;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double B9;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double B12;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double C;

    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double Potassium;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double Calcium;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double Phosphorus;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double Magnesium;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double Iron;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double Zinc;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double Fluorine;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double Manganese;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double Copper;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double Iodine;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double Selenium;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double Molybdenum;
    @DecimalMin(value = FoodPyramidConsts.MIN_DECIMAL, groups = {OnCreate.class, OnUpdate.class})
    private double Chromium;
}
