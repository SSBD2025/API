package pl.lodz.p.it.ssbd2025.ssbd02.utils.consts;

public class EmailConsts {
    public static final String TEMPLATE_EMAIL = "emailTemplate.html";
    public static final String TEMPLATE_CHANGE_EMAIL = "changeEmailTemplate.html";
    public static final String TEMPLATE_ADMIN_LOGIN = "emailAdminLoginTemplate.html";
    public static final String TEMPLATE_TWO_FACTOR = "twoFactorTemplate.html";
    public static final String TEMPLATE_ADMIN_CHANGED_PASSWORD = "adminChangedPassword.html";

    public static final String PLACEHOLDER_WELCOME = "${welcome}";
    public static final String PLACEHOLDER_NAME = "${name}";
    public static final String PLACEHOLDER_BODY = "${body}";
    public static final String PLACEHOLDER_URL = "${url}";
    public static final String PLACEHOLDER_LINK_TEXT = "${linkText}";
    public static final String PLACEHOLDER_IP_ADDR = "${ip_addr}";
    public static final String PLACEHOLDER_CODE = "${code}";
    public static final String PLACEHOLDER_MANUALLY = "${manually}";

    public static final String RESET_PASSWORD_URL_LOCAL = "http://localhost:5173/reset/password/";
    public static final String RESET_PASSWORD_URL_PROD = "https://team-2.proj-sum.it.p.lodz.pl/reset/password/";
}
