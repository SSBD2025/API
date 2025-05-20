package pl.lodz.p.it.ssbd2025.ssbd02.interceptors;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class LoginLoggerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoginLoggerInterceptor.class);

    @Before(value = "Pointcuts.loginMethod()")
    public Object getLoginAttemptInfo(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String login = (String) args[0];
        String ip = (String) args[2];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        String formattedDate = ZonedDateTime.now().format(formatter);
        String toLog = "[AUTH LOGGER] [" + formattedDate + "] User: " + login + " has attempted to log in with IP: " + ip;
        log.info(toLog);
        return toLog;
    }
}
