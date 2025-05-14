package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;

import java.util.Locale;
import java.util.ResourceBundle;

@MethodCallLogged
public class I18n {

    public static final String EMAIL_WELCOME = "email.welcome";
    public static final String EMAIL_DELETE_ACCOUNT_SUBJECT = "email.delete.account.subject";
    public static final String EMAIL_DELETE_ACCOUNT_BODY = "email.delete.account.body";
    public static final String EMAIL_VERIFICATION_REMINDER_SUBJECT = "email.verification.reminder.subject";
    public static final String EMAIL_VERIFICATION_REMINDER_BODY = "email.verification.reminder.body";

    public static String getMessage(String key, Language language) {
        String[] parts = language.name().split("_");
        Locale locale = new Locale.Builder().setLanguage(parts[0]).setRegion(parts[1]).build();

        ResourceBundle rb = ResourceBundle.getBundle("messages", locale);
        return rb.getString(key);
    }
}
