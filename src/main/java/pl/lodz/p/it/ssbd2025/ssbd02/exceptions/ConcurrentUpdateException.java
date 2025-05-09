package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class ConcurrentUpdateException extends AppBaseException {

    public ConcurrentUpdateException(Throwable cause) {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), "object modified since read for update", cause);
    }

    public ConcurrentUpdateException() {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), "object modified since read for update");
    }
}
