package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class TokenSignatureInvalidException extends AppBaseException {
    public TokenSignatureInvalidException() {
        super(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), "Token malformed or invalid signature");
    }
}