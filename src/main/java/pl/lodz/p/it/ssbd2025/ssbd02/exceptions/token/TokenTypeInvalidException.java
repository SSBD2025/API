package pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AppBaseException;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class TokenTypeInvalidException extends TokenBaseException {
    public TokenTypeInvalidException() {
        super(HttpStatusCode.valueOf(HttpStatus.FORBIDDEN.value()), ExceptionConsts.TOKEN_TYPE_INVALID);
    }
}