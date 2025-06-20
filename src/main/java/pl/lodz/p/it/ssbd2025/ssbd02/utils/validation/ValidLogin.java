package pl.lodz.p.it.ssbd2025.ssbd02.utils.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LoginValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLogin {
    String message() default "Login must contain only letters, numbers, dots, underscores, hyphens and cannot start or end with special characters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
