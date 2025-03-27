package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DietProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    private double Potassium;   // K
    private double Calcium;     // Ca
    private double Phosphorus;  // P
    private double Magnesium;   // Mg
    private double Iron;        // Fe
    private double Zinc;        // Zn
    private double Fluorine;    // F
    private double Manganese;   // Mn
    private double Copper;      // Cu
    private double Iodine;      // I
    private double Selenium;    // Se
    private double Molybdenum;  // Mo
    private double Chromium;    // Cr
}
