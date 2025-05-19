package pl.lodz.p.it.ssbd2025.ssbd02.utils.consts;

public class FeedbackConsts {
    public static final String TABLE_NAME = "feedback";
    public static final String COLUMN_CLIENT_ID = "client_id";
    public static final String COLUMN_FOOD_PYRAMID_ID = "food_pyramid_id";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public static final String CLIENT_ID_INDEX = "feedback_client_id_index";
    public static final String FOOD_PYRAMID_ID_INDEX = "feedback_food_pyramid_id_index";

    public static final int RATING_MIN = 1;
    public static final int RATING_MAX = 5;
    public static final int DESCRIPTION_MIN = 1;
    public static final int DESCRIPTION_MAX = 255;

    public static final String FIELD_CLIENT = "client";
    public static final String FIELD_FOOD_PYRAMID = "foodPyramid";
}
