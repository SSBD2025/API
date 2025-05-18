package pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AppBaseException;

public class TokenMalformedException extends TokenBaseException {
    public TokenMalformedException() {
      super(HttpStatusCode.valueOf(HttpStatus.UNAUTHORIZED.value()), "Token malformed");
    }
}