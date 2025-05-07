package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class AccountEmailAlreadyInUseException extends AppBaseException {
    public AccountEmailAlreadyInUseException() {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), "Email Already In Use.");
    }
}
