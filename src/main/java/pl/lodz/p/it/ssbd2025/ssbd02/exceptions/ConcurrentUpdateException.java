package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class ConcurrentUpdateException extends AppBaseException {

    public ConcurrentUpdateException(Throwable cause) {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), ExceptionConsts.CONCURRENT_UPDATE, cause);
    }

    public ConcurrentUpdateException() {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), ExceptionConsts.CONCURRENT_UPDATE);
    }
}
