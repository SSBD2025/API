package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.MissingHttpRequestException;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

        String lower = "abcdefghijklmnopqrstuvwxyz";
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String special = "!@#$%^&*()";

        String allAllowed = lower + upper + digits + special;

        List<Character> passwordChars = new ArrayList<>();

        passwordChars.add(lower.charAt(random.nextInt(lower.length())));
        passwordChars.add(upper.charAt(random.nextInt(upper.length())));
        passwordChars.add(digits.charAt(random.nextInt(digits.length())));
        passwordChars.add(special.charAt(random.nextInt(special.length())));

        for (int i = passwordChars.size(); i < length; i++) {
            passwordChars.add(allAllowed.charAt(random.nextInt(allAllowed.length())));
        }

        Collections.shuffle(passwordChars, random);

        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }
}
