package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class UnknownFilterException extends AppBaseException {
    public UnknownFilterException(Throwable cause) {
      super(HttpStatusCode.valueOf(HttpStatus.UNAUTHORIZED.value()), "Unknown filter exception occurred" + cause);
    }
}