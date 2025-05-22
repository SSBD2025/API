package pl.lodz.p.it.ssbd2025.ssbd02.interceptors;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.AccessRole;

import java.util.UUID;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class RoleChangeLoggingInterceptor {

    private static final Logger log = LoggerFactory.getLogger("RoleChangeLogger");

    @After("execution(* pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations.AccountService.assignRole(..)) || " +
            "execution(* pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations.AccountService.unassignRole(..))")
    public void logRoleChange(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length >= 3) {
            UUID accountId = (UUID) args[0];
            AccessRole accessRole = (AccessRole) args[1];
            String adminLogin = (String) args[2];

            String methodName = joinPoint.getSignature().getName();
            String operation = methodName.equals("assignRole") ? "assigned to" : "unassigned from";

            log.info("[ROLE ASSIGNMENT LOGGER] Role {} {} user with ID {} by administrator {}",
                    accessRole.name(), operation, accountId, adminLogin);
        }
    }
}
