package pl.lodz.p.it.ssbd2025.ssbd02.utils.consts;

public class BloodTestConsts {
    public static final String FIELD_CLIENT = "client";

    public static final String TABLE_NAME = "blood_test_results";
    public static final String COLUMN_RESULT = "result";
    public static final String COLUMN_BLOOD_MARKER = "blood_marker";
    public static final String COLUMN_REPORT_ID = "report_id";
    public static final String REPORT_ID_INDEX = "btr_report_id_index";

    public static final String CLIENT_REPORT_TABLE_NAME = "client_blood_test_reports";
    public static final String COLUMN_CLIENT_ID = "client_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String CLIENT_ID_INDEX = "cbtr_client_id_index";

    public static final String RESULT_MIN = "0.0";

    public static final int RESULTS_MIN_SIZE = 1;
    public static final int RESULTS_MAX_SIZE = 31;
}
