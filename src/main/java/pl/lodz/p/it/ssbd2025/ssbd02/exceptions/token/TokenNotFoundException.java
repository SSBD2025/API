package pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AppBaseException;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class TokenNotFoundException extends TokenBaseException {
    public TokenNotFoundException() {
        super(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), ExceptionConsts.TOKEN_NOT_FOUND);
    }
}