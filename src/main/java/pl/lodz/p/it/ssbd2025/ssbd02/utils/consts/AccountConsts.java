package pl.lodz.p.it.ssbd2025.ssbd02.utils.consts;

public class AccountConsts {
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()]).+$";
    public static final String PASSWORD_MESSAGE = "Password must contain a lowercase letter, an uppercase letter, a digit, and a special character.";

    public static final int LOGIN_MIN = 4;
    public static final int LOGIN_MAX = 50;
}
