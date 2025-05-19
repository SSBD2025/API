package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;

public class MissingHttpRequestException extends AppBaseException {
    public MissingHttpRequestException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "No active HTTP request context found. Cannot access request-scoped data.");
    }
}

