package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.FoodPyramidConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.FoodPyramidConsts;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodPyramidDTO {
    private UUID id;

    private double averageRating;

    private String name;

    private int kcal;

    private double fat;
    private double saturatedFattyAcids;
    private double carbohydrates;
    private double sugar;
    private double protein;

    private double A;
    private double D;
    private double E;
    private double K;
    private double B1;
    private double B2;
    private double B3;
    private double B5;
    private double B6;
    private double B7;
    private double B9;
    private double B12;
    private double C;

    private double Potassium;
    private double Calcium;
    private double Phosphorus;
    private double Magnesium;
    private double Iron;
    private double Zinc;
    private double Fluorine;
    private double Manganese;
    private double Copper;
    private double Iodine;
    private double Selenium;
    private double Molybdenum;
    private double Chromium;
}
