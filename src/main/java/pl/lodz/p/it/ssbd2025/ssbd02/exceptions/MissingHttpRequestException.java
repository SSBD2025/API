package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class MissingHttpRequestException extends AppBaseException {
    public MissingHttpRequestException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionConsts.MISSING_HTTP_REQUEST);
    }
}

