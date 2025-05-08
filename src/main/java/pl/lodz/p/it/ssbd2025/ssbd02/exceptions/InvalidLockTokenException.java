package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class InvalidLockTokenException extends AppBaseException {
    public InvalidLockTokenException() {
        super(HttpStatusCode.valueOf(HttpStatus.FORBIDDEN.value()), "Invalid Lock Token");
    }
}
