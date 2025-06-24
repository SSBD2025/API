package pl.lodz.p.it.ssbd2025.ssbd02.interceptors;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RollbackUnknown {

}
