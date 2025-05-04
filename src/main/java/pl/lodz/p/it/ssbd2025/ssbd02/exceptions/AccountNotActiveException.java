package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class AccountNotActiveException extends AppBaseException {
    public AccountNotActiveException() {
        super(HttpStatusCode.valueOf(HttpStatus.FORBIDDEN.value()), "Account not active");
    }
}
