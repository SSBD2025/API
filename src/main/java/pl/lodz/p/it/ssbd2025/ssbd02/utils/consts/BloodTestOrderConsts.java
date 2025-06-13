package pl.lodz.p.it.ssbd2025.ssbd02.utils.consts;

public class BloodTestOrderConsts {
    public static final String TABLE_NAME = "blood_test_order";
    public static final String PARAMETERS_TABLE_NAME = "blood_test_order_parameters";

    public static final String COLUMN_CLIENT_ID = "client_id";
    public static final String COLUMN_DIETITIAN_ID = "dietitian_id";
    public static final String COLUMN_ORDER_DATE = "order_date";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_ORDER_ID = "order_id";
    public static final String COLUMN_PARAMETER = "parameter";
    public static final String COLUMN_FULFILLED = "fulfilled";
    public static final String FIELD_CLIENT = "client";
    public static final String FIELD_DIETICIAN = "dietician";

    public static final String CLIENT_ID_INDEX = "blood_test_order_client_id_idx";
    public static final String DIETITIAN_ID_INDEX = "blood_test_order_dietitian_id_idx";
}
