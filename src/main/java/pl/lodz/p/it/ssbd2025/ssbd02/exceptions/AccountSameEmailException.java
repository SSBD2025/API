package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class AccountSameEmailException extends AppBaseException {
    public AccountSameEmailException() {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), "The same e-mail address was provided.");
    }
}
