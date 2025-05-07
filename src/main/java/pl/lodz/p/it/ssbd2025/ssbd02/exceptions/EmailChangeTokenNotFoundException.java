package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class EmailChangeTokenNotFoundException extends AppBaseException {
    public EmailChangeTokenNotFoundException() {
        super(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), "No valid email change token found for this account");
    }
}
