package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class TwoFactorTokenInvalidException extends AppBaseException {
    public TwoFactorTokenInvalidException() {
        super(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), "Two factor token is invalid");
    }
}
