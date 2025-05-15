package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LoggingLifecycleHooks {

    private static final Logger log = LoggerFactory.getLogger("StartupShutdownLogger");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    @PostConstruct
    public void onStartup() {
        log.info("[LOGGING STARTED] on {}", ZonedDateTime.now().format(formatter));
    }

    @PreDestroy
    public void onShutdown() {
        log.info("[LOGGING STOPPED] on {}", ZonedDateTime.now().format(formatter));
    }
}