package pl.lodz.p.it.ssbd2025.ssbd02.utils.consts;

public class AccountConsts {
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()]).+$";
    public static final String PASSWORD_MESSAGE = "Password must contain a lowercase letter, an uppercase letter, a digit, and a special character.";
    public static final String EMAIL_REGEX = "^((?!\\.)[\\w\\-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])$";
    public static final String EMAIL_MESSAGE = "Email must be valid.";
    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 60;
    public static final int LOGIN_MIN = 4;
    public static final int LOGIN_MAX = 50;

    public static final int EMAIL_MAX = 128;
    public static final int EMAIL_MIN = 0;

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

    public static final String NAME_REGEX = "^[a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ\\s'-]+$";
    public static final String NAME_MESSAGE = "Name can only contain letters, spaces, hyphens and apostrophes";

    public static final int MAX_LOGIN_ATTEMPTS = 10;
    public static final int MAX_PASSWORD_HISTORY = 10;

    public static final String LOGIN_NOT_BLANK_MESSAGE = "Login cannot be blank";
    public static final String LOGIN_SIZE_MESSAGE = "Login must be between " + LOGIN_MIN + " and " + LOGIN_MAX + " characters";

    public static final String PASSWORD_NOT_BLANK_MESSAGE = "Password cannot be blank";
    public static final String PASSWORD_SIZE_MESSAGE = "Password must be between " + PASSWORD_MIN + " and " + PASSWORD_MAX + " characters";

    public static final String EMAIL_NOT_BLANK_MESSAGE = "Email cannot be blank";
    public static final String EMAIL_SIZE_MESSAGE = "Email cannot exceed " + EMAIL_MAX + " characters";

    public static final String FIRST_NAME_NOT_BLANK_MESSAGE = "First name cannot be blank";
    public static final String FIRST_NAME_SIZE_MESSAGE = "First name must be between " + NAME_MIN + " and " + NAME_MAX + " characters";

    public static final String LAST_NAME_NOT_BLANK_MESSAGE = "Last name cannot be blank";
    public static final String LAST_NAME_SIZE_MESSAGE = "Last name must be between " + NAME_MIN + " and " + NAME_MAX + " characters";

    public static final String ACTIVE_NOT_NULL_MESSAGE = "Active status cannot be null";
    public static final String VERIFIED_NOT_NULL_MESSAGE = "Verified status cannot be null";
    public static final String TWO_FACTOR_AUTH_NOT_NULL_MESSAGE = "Two factor auth status cannot be null";
    public static final String REMINDED_NOT_NULL_MESSAGE = "Reminded status cannot be null";
    public static final String LOGIN_ATTEMPTS_NOT_NULL_MESSAGE = "Login attempts cannot be null";
    public static final String LOGIN_ATTEMPTS_MIN_MESSAGE = "Login attempts cannot be negative";
    public static final String LOGIN_ATTEMPTS_MAX_MESSAGE = "Login attempts cannot exceed " + MAX_LOGIN_ATTEMPTS;
    public static final String LOCKED_UNTIL_FUTURE_MESSAGE = "Lock expiration must be in the future";
    public static final String AUTO_LOCKED_NOT_NULL_MESSAGE = "Auto locked status cannot be null";
    public static final String PASSWORD_HISTORY_SIZE_MESSAGE = "Password history cannot exceed " + MAX_PASSWORD_HISTORY + " entries";
    public static final String EMAIL_INVALID_MESSAGE = "Email must be valid";
    public static final String LAST_SUCCESSFUL_LOGIN_PAST_OR_PRESENT = "Last successful login cannot be in the future";
    public static final String LAST_FAILED_LOGIN_PAST_OR_PRESENT = "Last failed login cannot be in the future";
    public static final String IP_SIZE_MESSAGE = "IP address cannot exceed " + IP_MAX + " characters";
}
