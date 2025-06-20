package pl.lodz.p.it.ssbd2025.ssbd02.utils.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class IpAddressValidator implements ConstraintValidator<ValidIpAddress, String> {

    private static final String IPV4_PATTERN =
            "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final String IPV6_PATTERN =
            "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +
                    "^::1$|^::$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,7}:$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,5}(?::[0-9a-fA-F]{1,4}){1,2}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,4}(?::[0-9a-fA-F]{1,4}){1,3}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,3}(?::[0-9a-fA-F]{1,4}){1,4}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,2}(?::[0-9a-fA-F]{1,4}){1,5}$|" +
                    "^[0-9a-fA-F]{1,4}:(?::[0-9a-fA-F]{1,4}){1,6}$|" +
                    "^:(?::[0-9a-fA-F]{1,4}){1,7}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,7}:$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){0,6}(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final Pattern IPV4_COMPILED = Pattern.compile(IPV4_PATTERN);
    private static final Pattern IPV6_COMPILED = Pattern.compile(IPV6_PATTERN);

    private boolean allowEmpty;

    @Override
    public void initialize(ValidIpAddress constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
    }

    @Override
    public boolean isValid(String ip, ConstraintValidatorContext context) {
        if (ip == null || ip.trim().isEmpty()) {
            return allowEmpty;
        }

        String trimmedIp = ip.trim();

        if (IPV4_COMPILED.matcher(trimmedIp).matches()) {
            return true;
        }

        if (IPV6_COMPILED.matcher(trimmedIp).matches()) {
            return true;
        }

        return false;
    }
}