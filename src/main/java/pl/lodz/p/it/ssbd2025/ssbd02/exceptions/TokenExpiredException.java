package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class TokenExpiredException extends AppBaseException {
    public TokenExpiredException() {
        super(HttpStatusCode.valueOf(HttpStatus.UNAUTHORIZED.value()), ExceptionConsts.TOKEN_EXPIRED);
    }
}
