package pl.lodz.p.it.ssbd2025.ssbd02.utils.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LoginValidator implements ConstraintValidator<ValidLogin, String> {

    private static final String LOGIN_PATTERN = "^[a-zA-Z0-9][a-zA-Z0-9._-]*[a-zA-Z0-9]$|^[a-zA-Z0-9]$";

    @Override
    public void initialize(ValidLogin constraintAnnotation) {}

    @Override
    public boolean isValid(String login, ConstraintValidatorContext context) {
        if (login == null || login.trim().isEmpty()) {
            return false;
        }

        return login.matches(LOGIN_PATTERN);
    }
}
