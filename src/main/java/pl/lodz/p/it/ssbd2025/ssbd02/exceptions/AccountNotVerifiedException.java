package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class AccountNotVerifiedException extends AppBaseException {
    public AccountNotVerifiedException() {
        super(HttpStatusCode.valueOf(HttpStatus.FORBIDDEN.value()), "Account not verified");
    }
}
