package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class AccountIsAutolockedException extends AppBaseException {
    public AccountIsAutolockedException() {
        super(HttpStatusCode.valueOf(HttpStatus.UNAUTHORIZED.value()), ExceptionConsts.ACCOUNT_IS_AUTOLOCKED);
    }
}
