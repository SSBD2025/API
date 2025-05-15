package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class CookieNotFoundException extends AppBaseException {
    public CookieNotFoundException() {
        super(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), "Cookie not found");
    }
}