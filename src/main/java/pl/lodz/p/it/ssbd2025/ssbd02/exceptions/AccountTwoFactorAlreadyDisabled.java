package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class AccountTwoFactorAlreadyDisabled extends AppBaseException {
    public AccountTwoFactorAlreadyDisabled() {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), "TODO"); //TODO
    }
}
