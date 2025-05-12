package pl.lodz.p.it.ssbd2025.ssbd02.interceptors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LogSanitizer;

@Aspect @Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class MethodCallLoggingInterceptor {

    private static final Logger log = LoggerFactory.getLogger(MethodCallLoggingInterceptor.class);

    @Around("Pointcuts.methodCallLoggedAnnotatedMethods() || Pointcuts.allRepositoryMethods()")
    public Object getOperationInfo(ProceedingJoinPoint joinPoint) throws Throwable{

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "--ANONYMOUS--";

        StringBuilder message = new StringBuilder("Method call ");
        if(null != RetrySynchronizationManager.getContext())
            message.append("(retry # ").append(RetrySynchronizationManager.getContext().getRetryCount()).append(") ");
        Object result;
        try {
            try {
                message.append("| ").append(joinPoint.getSignature().toLongString())
                        .append(":").append(String.valueOf(Thread.currentThread().threadId()));
                message.append("| user: ").append(username);
                message.append("| args: ");
                for (Object arg : joinPoint.getArgs()) {
                    Object sanitizedArg = LogSanitizer.sanitize(arg);
                    message.append(String.valueOf(sanitizedArg)).append(" ");
                }
            } catch (Exception e) {
                log.error("| Unexpected exception within interceptor: ", e);
                throw e;
            }

            result = joinPoint.proceed();

        } catch (Throwable t) {
            message.append("| thrown exception: ").append(t.toString());
            log.warn(message.toString(), t);
            throw t;
        }

        Object sanitizedResult = LogSanitizer.sanitize(result);
        message.append("| returned: ").append(String.valueOf(sanitizedResult)).append(" ");

        log.info(message.toString());

        return result;

    }


}