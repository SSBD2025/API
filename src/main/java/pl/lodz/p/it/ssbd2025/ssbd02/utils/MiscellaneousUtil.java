package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import jakarta.servlet.http.HttpServletRequest;

public class MiscellaneousUtil {
    public static String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0]; // In case of multiple IPs
    }
}
