package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BloodParameter {
    HGB("Hemoglobin", Unit.G_DL),
    HCT("Hematocrit", Unit.PERCENT),
    RBC("Red Blood Cells", Unit.X10_6_U_L),
    MCV("Mean Corpuscular Volume", Unit.FL),
    MCH("Mean Corpuscular Hemoglobin", Unit.PG),
    MCHC("Mean Corpuscular Hemoglobin Concentration", Unit.PERCENT),
    RDW("Red Cell Distribution Width", Unit.PERCENT),
    WBC("White Blood Cells", Unit.X10_6_U_L),
    EOS("Eosinophils", Unit.PERCENT),
    BASO("Basophils", Unit.PERCENT),
    LYMPH("Lymphocytes", Unit.PERCENT),
    MONO("Monocytes", Unit.PERCENT),
    PLT("Platelets", Unit.X10_6_U_L),
    MPV("Mean Platelet Volume", Unit.FL),
    PDW("Platelet Distribution Width", Unit.FL),
    PCT("Plateletcrit", Unit.PERCENT),
    P_LCR("Platelet Large Cell Ratio", Unit.PERCENT);

    private final String description;
    private final Unit unit;
}