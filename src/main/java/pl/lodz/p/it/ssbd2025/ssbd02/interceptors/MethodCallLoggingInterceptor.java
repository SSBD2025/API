package pl.lodz.p.it.ssbd2025.ssbd02.interceptors;

import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AppBaseException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Aspect @Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class MethodCallLoggingInterceptor {

    private static final Logger log = LoggerFactory.getLogger(MethodCallLoggingInterceptor.class);

    @Around("Pointcuts.methodCallLoggedAnnotatedMethods() || Pointcuts.allRepositoryMethods()")
    public Object getOperationInfo(ProceedingJoinPoint joinPoint) throws Throwable{

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "--ANONYMOUS--";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        String formattedDate = ZonedDateTime.now().format(formatter);

        StringBuilder message = new StringBuilder("[LOGGER] [");
        message.append(formattedDate);
        message.append("] Method call");

        if(null != RetrySynchronizationManager.getContext())
            message.append("(retry # ").append(RetrySynchronizationManager.getContext().getRetryCount()).append(") ");

        Object result;
        HttpServletResponse response = null;

        try {
            try {
                message.append(" | ").append(joinPoint.getSignature().toLongString())
                        .append(":").append(String.valueOf(Thread.currentThread().threadId()));
                message.append(" | User: ").append(username);
                message.append(" | Args: ");
                CodeSignature methodSignature = (CodeSignature) joinPoint.getSignature();
                String[] paramNames = methodSignature.getParameterNames();
                Object[] args = joinPoint.getArgs();

                for (int i = 0; i < args.length; i++) {
                    String paramName = paramNames[i];
                    message.append(paramName).append("=").append(String.valueOf(args[i])).append(" ");
                }
                String classPackage = joinPoint.getSignature().getDeclaringType().getPackage().getName();

                if (classPackage.matches("pl\\.lodz\\.p\\.it\\.ssbd2025\\.ssbd02(\\.[^.]+)*\\.rest(\\.[^.]+)*")) {
                    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                    if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                        jakarta.servlet.http.HttpServletRequest request = servletRequestAttributes.getRequest();
                        response = servletRequestAttributes.getResponse();
                        String ip = request.getRemoteAddr();
                        String url = request.getRequestURL().toString();
                        String method = request.getMethod();
                        message.append(" | Request sender IP: ").append(ip);
                        message.append(" | Request URL: ").append(url);
                        message.append(" | Method: ").append(method);
                    }
                }
            } catch (Exception e) {
                log.error(" | Unexpected exception within interceptor: ", e);
                throw e;
            }

            result = joinPoint.proceed();

            if (response != null) {
                message.append(" | Status code: [").append(response.getStatus()).append("]");
            }

        } catch (Throwable t) {
            int statusCode = 500;

            if (t instanceof AppBaseException appException) {
                statusCode = appException.getStatusCode().value();
            }

            message.append(" | Status code: [").append(statusCode).append("]");
            message.append(" | Thrown exception: ").append(t.getMessage());
            log.warn(message.toString(), t.getMessage());
            throw t;
        }

        message.append(" | returned: ").append(joinPoint.getSignature().toShortString()).append("=").append(String.valueOf(result)).append(" ");

        log.info(message.toString());

        return result;
    }
}