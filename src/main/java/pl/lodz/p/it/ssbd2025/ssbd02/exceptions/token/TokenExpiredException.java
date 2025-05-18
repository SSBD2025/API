package pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AppBaseException;

public class TokenExpiredException extends TokenBaseException {
    public TokenExpiredException() {
        super(HttpStatusCode.valueOf(HttpStatus.UNAUTHORIZED.value()), ExceptionConsts.TOKEN_EXPIRED);
    }
}
