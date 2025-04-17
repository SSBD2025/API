package pl.lodz.p.it.ssbd2025.ssbd02.enums;

import java.math.BigDecimal;

public enum BloodParameter {
    HGB("Hemoglobin", Unit.G_DL, "12.0", "16.0", "13.5", "18.0"),
    HCT("Hematocrit", Unit.PERCENT, "33.0", "51.0", "37.0", "53.0"),
    RBC("Red Blood Cells", Unit.X10_6_U_L, "4.0", "5.2", "4.5", "5.9"),
    MCV("Mean Corpuscular Volume", Unit.FL, "80.0", "100.0", "80.0", "100.0"),
    MCH("Mean Corpuscular Hemoglobin", Unit.PG, "26.0", "34.0", "26.0", "34.0"),
    MCHC("Mean Corpuscular Hemoglobin Concentration", Unit.PERCENT, "32.0", "36.0", "32.0", "36.0"),
    RDW("Red Cell Distribution Width", Unit.PERCENT, "11.5", "13.1", "11.5", "13.1"),
    WBC("White Blood Cells", Unit.X10_6_U_L, "4.5", "11.0", "4.5", "11.0"),
    EOS("Eosinophils", Unit.PERCENT, "0.0", "3.0", "0.0", "3.0"),
    BASO("Basophils", Unit.PERCENT, "0.0", "1.0", "0.0", "1.0"),
    LYMPH("Lymphocytes", Unit.PERCENT, "24.0", "44.0", "24.0", "44.0"),
    MONO("Monocytes", Unit.PERCENT, "4.0", "10.0", "4.0", "10.0"),
    PLT("Platelets", Unit.X10_6_U_L, "150.0", "450.0", "150.0", "450.0"),
    MPV("Mean Platelet Volume", Unit.FL, "6.5", "10.0", "6.5", "10.0"),
    PDW("Platelet Distribution Width", Unit.FL, "9.8", "12.5", "9.8", "12.5"),
    PCT("Plateletcrit", Unit.PERCENT, "0.2", "0.4", "0.2", "0.4"),
    P_LCR("Platelet Large Cell Ratio", Unit.PERCENT, "19.1", "46.6", "19.1", "46.6");

    private final String description;
    private final Unit unit;
    private final BigDecimal womanStandardMin;
    private final BigDecimal womanStandardMax;
    private final BigDecimal menStandardMin;
    private final BigDecimal menStandardMax;

    BloodParameter(String description, Unit unit, String womanStandardMin, String womanStandardMax, String menStandardMin, String menStandardMax) {
        this.description = description;
        this.unit = unit;
        this.womanStandardMin = new BigDecimal(womanStandardMin);
        this.womanStandardMax = new BigDecimal(womanStandardMax);
        this.menStandardMin = new BigDecimal(menStandardMin);
        this.menStandardMax = new BigDecimal(menStandardMax);
    }

    public String getDescription() {
        return description;
    }

    public Unit getUnit() {
        return unit;
    }

    public BigDecimal getWomanStandardMin() {
        return womanStandardMin;
    }

    public BigDecimal getWomanStandardMax() {
        return womanStandardMax;
    }

    public BigDecimal getMenStandardMin() {
        return menStandardMin;
    }

    public BigDecimal getMenStandardMax() {
        return menStandardMax;
    }
}
