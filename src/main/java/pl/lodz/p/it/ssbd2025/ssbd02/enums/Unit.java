package pl.lodz.p.it.ssbd2025.ssbd02.enums;

public enum Unit {
    G_DL("g/dL"),
    PERCENT("%"),
    X10_12_U_L("x10^12/µl"),
    X10_9_U_L("x10^9/µl"),
    X10_6_U_L("x10^6/µl"),
    X10_3_U_L("x10^3/µl"),
    PG("pg"),
    FL("fl"),
    MU_ML("mU/ml");

    private final String symbol;

    Unit(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}