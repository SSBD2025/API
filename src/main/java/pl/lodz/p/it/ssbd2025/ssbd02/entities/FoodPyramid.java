package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FoodPyramid extends AbstractEntity {
    @Min(0)
    private double A;
    @Min(0)
    private double D;
    @Min(0)
    private double E;
    @Min(0)
    private double K;
    @Min(0)
    private double B1;
    @Min(0)
    private double B2;
    @Min(0)
    private double B3;
    @Min(0)
    private double B5;
    @Min(0)
    private double B6;
    @Min(0)
    private double B7;
    @Min(0)
    private double B9;
    @Min(0)
    private double B12;
    @Min(0)
    private double C;

    @Min(0)
    private double Potassium;   // K
    @Min(0)
    private double Calcium;     // Ca
    @Min(0)
    private double Phosphorus;  // P
    @Min(0)
    private double Magnesium;   // Mg
    @Min(0)
    private double Iron;        // Fe
    @Min(0)
    private double Zinc;        // Zn
    @Min(0)
    private double Fluorine;    // F
    @Min(0)
    private double Manganese;   // Mn
    @Min(0)
    private double Copper;      // Cu
    @Min(0)
    private double Iodine;      // I
    @Min(0)
    private double Selenium;    // Se
    @Min(0)
    private double Molybdenum;  // Mo
    @Min(0)
    private double Chromium;    // Cr
}
