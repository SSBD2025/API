package pl.lodz.p.it.ssbd2025.ssbd02.interceptors;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class UserRoleChangeLoggingInterceptor {

    private static final Logger log = LoggerFactory.getLogger("RoleChangeLogger");

    @After("Pointcuts.userRoleChangeLoggedAnnotatedMethods()")
    public void logUserRoleChange(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length >= 3) {
            String login = (String) args[0];
            String previousRole = (String) args[1];
            String newRole = (String) args[2];

            log.info("User {} changed active role from {} to {}",
                    login,
                    previousRole != null ? previousRole.toUpperCase() : "NONE",
                    newRole.toUpperCase());
        }
    }
}
