package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FoodPyramid extends AbstractEntity {

    @OneToMany(mappedBy = "foodPyramid")
    private List<ClientFoodPyramid> clientFoodPyramids = new ArrayList<>();

    @OneToMany(mappedBy = "foodPyramid", cascade = CascadeType.REFRESH)
    private List<Feedback> feedbacks = new ArrayList<>();

    @Min(0)
    @Column(nullable = false, updatable = false)
    private double A;
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double D;
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double E;
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double K;
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double B1;
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double B2;
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double B3;
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double B5;
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double B6;
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double B7;
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double B9;
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double B12;
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double C;

    @Min(0)
    @Column(nullable = false, updatable = false)
    private double Potassium;   // K
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double Calcium;     // Ca
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double Phosphorus;  // P
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double Magnesium;   // Mg
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double Iron;        // Fe
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double Zinc;        // Zn
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double Fluorine;    // F
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double Manganese;   // Mn
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double Copper;      // Cu
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double Iodine;      // I
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double Selenium;    // Se
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double Molybdenum;  // Mo
    @Min(0)
    @Column(nullable = false, updatable = false)
    private double Chromium;    // Cr
}
