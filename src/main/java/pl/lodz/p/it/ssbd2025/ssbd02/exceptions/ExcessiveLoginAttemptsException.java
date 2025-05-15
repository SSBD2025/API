package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class ExcessiveLoginAttemptsException extends AppBaseException {
    public ExcessiveLoginAttemptsException() {
        super(HttpStatusCode.valueOf(HttpStatus.FORBIDDEN.value()), "Due to an excessive amount of failed login attempts, account has been temporarily blocked");
    }
}
