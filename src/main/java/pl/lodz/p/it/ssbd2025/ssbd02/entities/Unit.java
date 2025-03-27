package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Unit {
    G_DL("g/dL"),
    PERCENT("%"),
    X10_6_U_L("x10^6/µl"),
    X10_3_U_L("x10^3/µl"),
    PG("pg"),
    FL("fl");

    private final String symbol;
}