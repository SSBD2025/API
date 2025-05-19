package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class AccountConstraintViolationException extends AppBaseException{
    public AccountConstraintViolationException(String key) {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), key);
    }
    public AccountConstraintViolationException() {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), ExceptionConsts.ACCOUNT_CONSTRAINT_VIOLATION);
    }
    public AccountConstraintViolationException(Throwable cause) {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), ExceptionConsts.ACCOUNT_CONSTRAINT_VIOLATION, cause);
    }

}
