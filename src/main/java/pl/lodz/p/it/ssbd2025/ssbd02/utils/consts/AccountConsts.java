package pl.lodz.p.it.ssbd2025.ssbd02.utils.consts;

public class AccountConsts {
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()]).+$";
    public static final String PASSWORD_MESSAGE = "Password must contain a lowercase letter, an uppercase letter, a digit, and a special character.";
    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 60;
    public static final int LOGIN_MIN = 4;
    public static final int LOGIN_MAX = 50;

    public static final int EMAIL_MAX = 128;

    public static final int NAME_MIN = 1;
    public static final int NAME_MAX = 50;

    public static final int IP_MAX = 45;

    public static final String TABLE_NAME = "account";
    public static final String COLUMN_LOGIN = "login";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_LANGUAGE = "language";
    public static final String COLUMN_LAST_SUCCESSFUL_LOGIN = "last_successful_login";
    public static final String COLUMN_LAST_FAILED_LOGIN = "last_failed_login";
    public static final String COLUMN_LAST_SUCCESSFUL_LOGIN_IP = "last_successful_login_ip";
    public static final String COLUMN_LAST_FAILED_LOGIN_IP = "last_failed_login_ip";
    public static final String COLUMN_TWO_FACTOR_AUTH = "two_factor_auth";
    public static final String COLUMN_REMINDED = "reminded";
    public static final String COLUMN_LOGIN_ATTEMPTS = "login_attempts";
    public static final String COLUMN_LOCKED_UNTIL = "locked_until";
    public static final String COLUMN_AUTO_LOCKED = "auto_locked";
    public static final String TABLE_NAME_PASSWORD_HISTORY = "account_password_history";
    public static final String COLUMN_OLD_PASSWORD = "old_password";

    public static final String LOGIN_INDEX = "login_index";
    public static final String EMAIL_INDEX = "email_index";

    public static final boolean DEFAULT_ACTIVE = false;
    public static final boolean DEFAULT_VERIFIED = false;
    public static final boolean DEFAULT_TWO_FACTOR_AUTH = false;
    public static final boolean DEFAULT_REMINDED = false;
    public static final int DEFAULT_LOGIN_ATTEMPTS = 0;
}
