package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.*;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.BloodParameter;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ClientBloodTestReportNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ClientNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.SurveyNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.*;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IAlgorithmService;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled=true)
public class AlgorithmService implements IAlgorithmService {

    private final ClientModRepository clientModRepository;
    private final FoodPyramidRepository foodPyramidRepository;
    private final PeriodicSurveyRepository periodicSurveyRepository;
    private final BloodTestResultRepository bloodTestResultRepository;
    private final ClientBloodTestReportRepository clientBloodTestReportRepository;

    @PreAuthorize("hasRole('DIETICIAN')")
    @Transactional(propagation = Propagation.REQUIRES_NEW,
            transactionManager = "modTransactionManager",
            timeoutString = "${transaction.timeout}")
    public FoodPyramid generateFoodPyramid(UUID clientId) {
        Client client = clientModRepository.findClientById(clientId).orElseThrow(ClientNotFoundException::new);

        if (client.getSurvey() == null) {
            throw new SurveyNotFoundException();
        }

        PeriodicSurvey latestSurvey = periodicSurveyRepository.findFirstByClientOrderByMeasurementDateDesc(client).orElseThrow(SurveyNotFoundException::new);

        //czesc A

        FoodPyramid basePyramid;

        if (client.getSurvey().isGender()) {
            if (calculateAge(client.getSurvey().getDateOfBirth()) < 40) {
                basePyramid = foodPyramidRepository.findByName("M20");
            }
            else {
                basePyramid = foodPyramidRepository.findByName("M40");
            }
        } else {
            if (calculateAge(client.getSurvey().getDateOfBirth()) < 40) {
                basePyramid = foodPyramidRepository.findByName("K20");
            }
            else {
                basePyramid = foodPyramidRepository.findByName("K40");
            }
        }

        //czesc B
        basePyramid.setProtein(0.8 * latestSurvey.getWeight());
        int BMR = (int) ((10 * latestSurvey.getWeight())
                + (6.25 * client.getSurvey().getHeight())
                - (5 * calculateAge(client.getSurvey().getDateOfBirth())));

        if (client.getSurvey().isGender())
            BMR += 5;
        else
            BMR -= 161;

        BMR = (int) (BMR * client.getSurvey().getActivityLevel().getCalorieModifier());
        BMR += client.getSurvey().getNutritionGoal().getCalorieModifier();
        basePyramid.setKcal(BMR);

        //czesc C

        if (client.getDietaryRestrictions() != null) {
            boolean isVegetarian = client.getDietaryRestrictions().isVegetarian();
            boolean isVegan = client.getDietaryRestrictions().isVegan();

            if (isVegan) {
                basePyramid.setB12(basePyramid.getB12() * 1.2);
                basePyramid.setD(basePyramid.getD() * 1.2);
                basePyramid.setZinc(basePyramid.getZinc() * 1.2);
            } else if (isVegetarian) {
                basePyramid.setB12(basePyramid.getB12() * 1.1);
                basePyramid.setD(basePyramid.getD() * 1.1);
                basePyramid.setZinc(basePyramid.getZinc() * 1.1);
            }
        }

        // CZESC D
        boolean isMan = client.getSurvey().isGender();
        ClientBloodTestReport latestBloodTestReport = clientBloodTestReportRepository.
                findFirstByClientOrderByTimestampDesc(client).orElseThrow(ClientBloodTestReportNotFoundException::new);

        //Żelazo
        latestBloodTestReport.getResults().stream()
                .filter(result -> result.getBloodParameter() == BloodParameter.IRON)
                .findFirst()
                .ifPresent(result -> {
                    Pair<Double, Double> standards = getStandardsByGender(result.getBloodParameter(), isMan);
                    if (standards.a > result.getResult()) {
                        basePyramid.setIron(1.2 * basePyramid.getIron());
                    }
                    if (standards.b < result.getResult()) {
                        basePyramid.setIron(0.8 * basePyramid.getIron());
                    }
                });
        //Anemia
        findLowResult(latestBloodTestReport, BloodParameter.HGB, isMan).flatMap(hgb ->
                findLowResult(latestBloodTestReport, BloodParameter.FERRITIN, isMan)).ifPresent(ferritin -> {
            basePyramid.setIron(1.2 * basePyramid.getIron());
            basePyramid.setB6(1.2 * basePyramid.getB6());
            basePyramid.setB9(1.1 * basePyramid.getB9());
            basePyramid.setB12(1.1 * basePyramid.getB12());
        });
        //Anemia megaloblastyczna
        findHighResult(latestBloodTestReport, BloodParameter.MCV, isMan)
                .flatMap(mcv ->
                        findLowResult(latestBloodTestReport, BloodParameter.B12, isMan)
                                .flatMap(b12 ->
                                        findLowResult(latestBloodTestReport, BloodParameter.B9, isMan)
                                )
                ).ifPresent(b9 -> {
                    // MCV high, B12 low, B9 low
                    basePyramid.setB12(1.2 * basePyramid.getB12());
                    basePyramid.setB9(1.1 * basePyramid.getB9());
                    basePyramid.setIron(1.1 * basePyramid.getIron());
                });
        //Osteoporoza
        findLowResult(latestBloodTestReport, BloodParameter.CA, isMan).ifPresent(
                ca -> {
                    basePyramid.setCalcium(1.2 * basePyramid.getCalcium());
                    basePyramid.setD(1.1 * basePyramid.getD());
                    basePyramid.setK(1.1 * basePyramid.getK());
                    basePyramid.setMagnesium(0.9 * basePyramid.getMagnesium());
                    basePyramid.setIron(0.9 * basePyramid.getIron());
                }
        );
        //Niska odporność
        findLowResult(latestBloodTestReport, BloodParameter.LYMPH, isMan)
                .flatMap(lymph ->
                        findLowResult(latestBloodTestReport, BloodParameter.D, isMan)
                                .flatMap(vitD ->
                                        findLowResult(latestBloodTestReport, BloodParameter.ZN, isMan)
                                )
                ).ifPresent(zn -> {

                    basePyramid.setC(1.1 * basePyramid.getC());
                    basePyramid.setD(1.2 * basePyramid.getD());
                    basePyramid.setZinc(1.1 * basePyramid.getZinc());
                });
        //Nadwaga / otyłość
        Double BMI = latestSurvey.getWeight() / (client.getSurvey().getHeight() * client.getSurvey().getHeight());
        if(BMI > 25) {
            findHighResult(latestBloodTestReport, BloodParameter.GLUCOSE, isMan).flatMap(glucose ->
                    findHighResult(latestBloodTestReport, BloodParameter.INSULIN, isMan)).ifPresent(insulin -> {
                basePyramid.setSugar(0.8 * basePyramid.getSugar());
            });
        }
        //Nadciśnienie
//        if(latestSurvey.getBloodPressure() ) //TODO bo jakiś śmieszek zrobił ciśnienie jako String!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //TODO

        // Cukrzyca typu 2
        findHighResult(latestBloodTestReport, BloodParameter.GLUCOSE, isMan).flatMap(glucose ->
                findHighResult(latestBloodTestReport, BloodParameter.INSULIN, isMan)).ifPresent(insulin -> {
            basePyramid.setSugar(0.8 * basePyramid.getSugar());
        });
        //miażdżyca
        findHighResult(latestBloodTestReport, BloodParameter.CHOL, isMan)
                .flatMap(chol ->
                        findHighResult(latestBloodTestReport, BloodParameter.LDL, isMan)
                                .flatMap(ldl ->
                                        findLowResult(latestBloodTestReport, BloodParameter.HDL, isMan)
                                )
                ).ifPresent(hdl -> {
                    basePyramid.setSaturatedFattyAcids(0.8 * basePyramid.getSaturatedFattyAcids());
                });
        //Hipowitaminoza D
        findLowResult(latestBloodTestReport, BloodParameter.OH_D, isMan).ifPresent(
                ca -> {
                    basePyramid.setD(1.2 * basePyramid.getD());
                    basePyramid.setMagnesium(1.1 * basePyramid.getMagnesium());
                    basePyramid.setK(1.1 * basePyramid.getK());
                }
        );
        //Depresja
        findLowResult(latestBloodTestReport, BloodParameter.D, isMan)
                .flatMap(vitD ->
                        findLowResult(latestBloodTestReport, BloodParameter.B6, isMan)
                ).ifPresent(b6 -> {
                    basePyramid.setB6(1.3 * basePyramid.getB6());
                    basePyramid.setD(1.2 * basePyramid.getD());
                    basePyramid.setMagnesium(1.1 * basePyramid.getMagnesium());
                });

        return basePyramid;
    }

    private int calculateAge(Timestamp birthTime) {
        LocalDate birthDate = birthTime.toLocalDateTime().toLocalDate();
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private Pair<Double, Double> getStandardsByGender(BloodParameter parameter, boolean isMan) {
        if (isMan) {
            return new Pair<>(parameter.getMenStandardMin(), parameter.getMenStandardMax());
        } else {
            return new Pair<>(parameter.getWomanStandardMin(), parameter.getWomanStandardMax());
        }
    }

    private Optional<BloodTestResult> findLowResult(ClientBloodTestReport report, BloodParameter parameter, boolean isMan) {
        return report.getResults().stream()
                .filter(result -> result.getBloodParameter() == parameter)
                .filter(result -> {
                    Pair<Double, Double> standards = getStandardsByGender(parameter, isMan);
                    return result.getResult() < standards.a;
                })
                .findFirst();
    }

    private Optional<BloodTestResult> findHighResult(ClientBloodTestReport report, BloodParameter parameter, boolean isMan) {
        return report.getResults().stream()
                .filter(result -> result.getBloodParameter() == parameter)
                .filter(result -> {
                    Pair<Double, Double> standards = getStandardsByGender(parameter, isMan);
                    return result.getResult() > standards.b;
                })
                .findFirst();
    }
}
