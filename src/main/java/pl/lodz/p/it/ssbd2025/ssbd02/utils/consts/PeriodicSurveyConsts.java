package pl.lodz.p.it.ssbd2025.ssbd02.utils.consts;

public class PeriodicSurveyConsts {
    public static final String TABLE_NAME = "periodic_survey";
    public static final String COLUMN_CLIENT_ID = "client_id";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_BLOOD_PRESSURE = "blood_pressure";
    public static final String COLUMN_BLOOD_SUGAR_LEVEL = "blood_sugar_level";
    public static final String COLUMN_MEASUREMENT_DATE = "measurement_date";

    public static final String CLIENT_ID_INDEX = "ps_client_id_index";

    public static final String WEIGHT_MIN = "20.0";
    public static final String WEIGHT_MAX = "350.0";
    public static final String BLOOD_SUGAR_MIN = "10.0";
    public static final String BLOOD_SUGAR_MAX = "500.0";
    public static final String BLOOD_PRESSURE_PATTERN = "(\\d{2,3})/(\\d{2,3})";
    public static final String BLOOD_PRESSURE_MESSAGE = "Blood pressure must be in the format 'XX/XX'";
    public static final String FIELD_CLIENT = "client";
}
