package pl.lodz.p.it.ssbd2025.ssbd02.utils.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IpAddressValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIpAddress {
    String message() default "Invalid IP address format (must be valid IPv4 or IPv6)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    boolean allowEmpty() default true;
}