package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.MissingHttpRequestException;

import java.security.SecureRandom;
import java.util.stream.Collectors;

public class MiscellaneousUtil {
    public static String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0]; // In case of multiple IPs
    }

    public static String getAcceptLanguage() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            throw new MissingHttpRequestException();
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        return request.getHeader("Accept-Language");
    }

    public static String generateRandomPassword() {
        final int length = 12;
        SecureRandom random = new SecureRandom();

        return random.ints(length,33, 127)
                .mapToObj(i -> String.valueOf((char) i))
                .collect(Collectors.joining());
    }
}
