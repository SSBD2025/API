package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "blood_markers")
@Immutable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodMarker {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_parameter", nullable = false)
    private BloodParameter bloodParameter;

    @Column(name = "woman_standard_min", nullable = false)
    private BigDecimal womanStandardMin;

    @Column(name = "woman_standard_max", nullable = false)
    private BigDecimal womanStandardMax;

    @Column(name = "men_standard_min", nullable = false)
    private BigDecimal menStandardMin;

    @Column(name = "men_standard_max", nullable = false)
    private BigDecimal menStandardMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false)
    private Unit unit;

    //TODO orphanRemoval = true
    @OneToMany(mappedBy = "bloodMarker", cascade = CascadeType.ALL)
    private List<BloodTestResult> bloodTestResults;

    public BloodMarker(BloodParameter bloodParameter, Unit unit) {
        this.bloodParameter = bloodParameter;
        this.unit = unit;

        switch (bloodParameter) {
            case HGB:
                this.womanStandardMin = new BigDecimal("12.0");
                this.womanStandardMax = new BigDecimal("16.0");
                this.menStandardMin = new BigDecimal("13.5");
                this.menStandardMax = new BigDecimal("18.0");
                break;
            case HCT:
                this.womanStandardMin = new BigDecimal("33.0");
                this.womanStandardMax = new BigDecimal("51.0");
                this.menStandardMin = new BigDecimal("37.0");
                this.menStandardMax = new BigDecimal("53.0");
                break;
            case RBC:
                this.womanStandardMin = new BigDecimal("4.0");
                this.womanStandardMax = new BigDecimal("5.2");
                this.menStandardMin = new BigDecimal("4.5");
                this.menStandardMax = new BigDecimal("5.9");
                break;
            case MCV:
                this.womanStandardMin = new BigDecimal("80.0");
                this.womanStandardMax = new BigDecimal("100.0");
                this.menStandardMin = new BigDecimal("80.0");
                this.menStandardMax = new BigDecimal("100.0");
                break;
            case MCH:
                this.womanStandardMin = new BigDecimal("26.0");
                this.womanStandardMax = new BigDecimal("34.0");
                this.menStandardMin = new BigDecimal("26.0");
                this.menStandardMax = new BigDecimal("34.0");
                break;
            case MCHC:
                this.womanStandardMin = new BigDecimal("32.0");
                this.womanStandardMax = new BigDecimal("36.0");
                this.menStandardMin = new BigDecimal("32.0");
                this.menStandardMax = new BigDecimal("36.0");
                break;
            case RDW:
                this.womanStandardMin = new BigDecimal("11.5");
                this.womanStandardMax = new BigDecimal("13.1");
                this.menStandardMin = new BigDecimal("11.5");
                this.menStandardMax = new BigDecimal("13.1");
                break;
            case WBC:
                this.womanStandardMin = new BigDecimal("4.5");
                this.womanStandardMax = new BigDecimal("11.0");
                this.menStandardMin = new BigDecimal("4.5");
                this.menStandardMax = new BigDecimal("11.0");
                break;
            case EOS:
                this.womanStandardMin = new BigDecimal("0.0");
                this.womanStandardMax = new BigDecimal("3.0");
                this.menStandardMin = new BigDecimal("0.0");
                this.menStandardMax = new BigDecimal("3.0");
                break;
            case BASO:
                this.womanStandardMin = new BigDecimal("0.0");
                this.womanStandardMax = new BigDecimal("1.0");
                this.menStandardMin = new BigDecimal("0.0");
                this.menStandardMax = new BigDecimal("1.0");
                break;
            case LYMPH:
                this.womanStandardMin = new BigDecimal("24.0");
                this.womanStandardMax = new BigDecimal("44.0");
                this.menStandardMin = new BigDecimal("24.0");
                this.menStandardMax = new BigDecimal("44.0");
                break;
            case MONO:
                this.womanStandardMin = new BigDecimal("4.0");
                this.womanStandardMax = new BigDecimal("10.0");
                this.menStandardMin = new BigDecimal("4.0");
                this.menStandardMax = new BigDecimal("10.0");
                break;
            case PLT:
                this.womanStandardMin = new BigDecimal("150.0");
                this.womanStandardMax = new BigDecimal("450.0");
                this.menStandardMin = new BigDecimal("150.0");
                this.menStandardMax = new BigDecimal("450.0");
                break;
            case MPV:
                this.womanStandardMin = new BigDecimal("6.5");
                this.womanStandardMax = new BigDecimal("10.0");
                this.menStandardMin = new BigDecimal("6.5");
                this.menStandardMax = new BigDecimal("10.0");
                break;
            case PDW:
                this.womanStandardMin = new BigDecimal("9.8");
                this.womanStandardMax = new BigDecimal("12.5");
                this.menStandardMin = new BigDecimal("9.8");
                this.menStandardMax = new BigDecimal("12.5");
                break;
            case PCT:
                this.womanStandardMin = new BigDecimal("0.2");
                this.womanStandardMax = new BigDecimal("0.4");
                this.menStandardMin = new BigDecimal("0.2");
                this.menStandardMax = new BigDecimal("0.4");
                break;
            case P_LCR:
                this.womanStandardMin = new BigDecimal("19.1");
                this.womanStandardMax = new BigDecimal("46.6");
                this.menStandardMin = new BigDecimal("19.1");
                this.menStandardMax = new BigDecimal("46.6");
                break;
            default:
                throw new IllegalArgumentException("Unknown blood parameter");
        }
    }
}