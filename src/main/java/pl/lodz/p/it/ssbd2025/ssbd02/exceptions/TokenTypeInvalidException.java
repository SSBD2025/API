package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class TokenTypeInvalidException extends AppBaseException {
    public TokenTypeInvalidException() {
        super(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), "Token type invalid");
    }
}