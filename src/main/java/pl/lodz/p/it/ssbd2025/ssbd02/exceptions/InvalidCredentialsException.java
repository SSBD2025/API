package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class InvalidCredentialsException extends AppBaseException {
    public InvalidCredentialsException() {
        super(HttpStatusCode.valueOf(HttpStatus.UNAUTHORIZED.value()), "Provided credentials are invalid");
    }
}
