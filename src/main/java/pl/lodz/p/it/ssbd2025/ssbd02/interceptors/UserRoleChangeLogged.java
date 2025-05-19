package pl.lodz.p.it.ssbd2025.ssbd02.interceptors;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface UserRoleChangeLogged {}
