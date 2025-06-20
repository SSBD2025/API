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

    private double averageRating;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Size(min = FoodPyramidConsts.MIN_NAME, max = FoodPyramidConsts.MAX_NAME, groups = {OnCreate.class, OnUpdate.class})
    private String name;

    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private int kcal;

    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double fat;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double saturatedFattyAcids;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double carbohydrates;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double sugar;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double protein;

    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double A;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double D;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double E;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double K;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double B1;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double B2;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double B3;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double B5;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double B6;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double B7;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double B9;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double B12;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double C;

    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double Potassium;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double Calcium;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double Phosphorus;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double Magnesium;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double Iron;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double Zinc;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double Fluorine;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double Manganese;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double Copper;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double Iodine;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double Selenium;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double Molybdenum;
    @Min(value = FoodPyramidConsts.MIN_VALUE, groups = {OnCreate.class, OnUpdate.class})
    private double Chromium;
}
