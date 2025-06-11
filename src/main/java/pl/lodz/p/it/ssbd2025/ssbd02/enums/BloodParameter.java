package pl.lodz.p.it.ssbd2025.ssbd02.enums;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum BloodParameter {
    HGB("Hemoglobin", Unit.G_DL, 12.0, 16.0, 13.5, 18.0),
    HCT("Hematocrit", Unit.PERCENT, 33.0, 51.0, 37.0, 53.0),
    RBC("Red Blood Cells", Unit.X10_6_U_L, 4.0, 5.2, 4.5, 5.9),
    MCV("Mean Corpuscular Volume", Unit.FL, 80.0, 100.0, 80.0, 100.0),
    MCH("Mean Corpuscular Hemoglobin", Unit.PG, 26.0, 34.0, 26.0, 34.0),
    MCHC("Mean Corpuscular Hemoglobin Concentration", Unit.PERCENT, 32.0, 36.0, 32.0, 36.0),
    RDW("Red Cell Distribution Width", Unit.PERCENT, 11.5, 13.1, 11.5, 13.1),
    WBC("White Blood Cells", Unit.X10_6_U_L, 4.5, 11.0, 4.5, 11.0),
    EOS("Eosinophils", Unit.PERCENT, 0.0, 3.0, 0.0, 3.0),
    BASO("Basophils", Unit.PERCENT, 0.0, 1.0, 0.0, 1.0),
    LYMPH("Lymphocytes", Unit.PERCENT, 24.0, 44.0, 24.0, 44.0),
    MONO("Monocytes", Unit.PERCENT, 4.0, 10.0, 4.0, 10.0),
    PLT("Platelets", Unit.X10_6_U_L, 150.0, 450.0, 150.0, 450.0),
    MPV("Mean Platelet Volume", Unit.FL, 6.5, 10.0, 6.5, 10.0),
    PDW("Platelet Distribution Width", Unit.FL, 9.8, 12.5, 9.8, 12.5),
    PCT("Plateletcrit", Unit.PERCENT, 0.2, 0.4, 0.2, 0.4),
    P_LCR("Platelet Large Cell Ratio", Unit.PERCENT, 19.1, 46.6, 19.1, 46.6),
    IRON("Iron", Unit.X10_6_U_L, 55.0, 180.0, 70.0, 200.0),
    FERRITIN("Ferritin", Unit.X10_6_U_L, 10.0, 200.0, 15.0, 400.0),
    B9("Folic acid", Unit.X10_9_U_L, 6.0, 20.0, 6.0, 20.0),
    D("Vitamin D", Unit.X10_9_U_L, 20.0, 50.0, 20.0, 50.0),
    B12("Vitamin B12", Unit.X10_12_U_L, 200.0, 900.0, 200.0, 900.0),
    GLUCOSE("Glucose", Unit.X10_3_U_L, 70.0, 99.0, 70.0, 99.0),
    INSULIN("Insulin", Unit.X10_3_U_L, 3.0, 17.0, 3.0, 17.0),
    CHOL("Cholesterol", Unit.X10_3_U_L, 114.0, 190.0, 114.0, 190.0),
    CA("Calcium", Unit.X10_3_U_L, 8.5, 10.5, 8.5, 10.5),
    ZN("Zinc", Unit.X10_6_U_L, 70.0, 160.0, 70.0, 160.0),
    LDL("Low-Density Lipoproteins", Unit.X10_3_U_L, 0., 135., 0., 135.0),
    HDL("High-Density Lipoproteins", Unit.X10_3_U_L, 40., 999., 40., 999.0),
    OH_D("25-Hydroxyvitamin D", Unit.X10_9_U_L, 30., 50., 30., 50.),
    B6("Vitamin B6", Unit.X10_6_U_L, 5.0, 30.0, 5.0, 30.0),;


    private final String description;
    private final Unit unit;
    private final Double womanStandardMin;
    private final Double womanStandardMax;
    private final Double menStandardMin;
    private final Double menStandardMax;

    BloodParameter(String description, Unit unit, Double womanStandardMin, Double womanStandardMax, Double menStandardMin, Double menStandardMax) {
        this.description = description;
        this.unit = unit;
        this.womanStandardMin = womanStandardMin;
        this.womanStandardMax = womanStandardMax;
        this.menStandardMin = menStandardMin;
        this.menStandardMax = menStandardMax;
    }

}
