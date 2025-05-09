package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class AccountConstraintViolationException extends AppBaseException{
    public AccountConstraintViolationException(String key) {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), key);
    }
    public AccountConstraintViolationException() {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), "Unspecified account constraint violation");
    }
    public AccountConstraintViolationException(Throwable cause) {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), "Unspecified account constraint violation", cause);
    }

}
