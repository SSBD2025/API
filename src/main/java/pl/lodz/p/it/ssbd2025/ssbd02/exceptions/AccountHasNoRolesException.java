package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class AccountHasNoRolesException extends AppBaseException {
    public AccountHasNoRolesException() {
        super(HttpStatusCode.valueOf(HttpStatus.FORBIDDEN.value()), "Account has no active roles");
    }
}
