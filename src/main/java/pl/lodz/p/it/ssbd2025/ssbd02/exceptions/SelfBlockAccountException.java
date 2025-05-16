package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class SelfBlockAccountException extends AppBaseException {
    public SelfBlockAccountException() {
      super(HttpStatusCode.valueOf(HttpStatus.FORBIDDEN.value()), "You cannot block your account");
    }
}
