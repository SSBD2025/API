package pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AppBaseException;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class TokenSignatureInvalidException extends TokenBaseException {
    public TokenSignatureInvalidException() {
        super(HttpStatusCode.valueOf(HttpStatus.UNAUTHORIZED.value()), ExceptionConsts.TOKEN_SIGNATURE_INVALID);
    }
}