package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ChangePasswordConsts;

public class PasswordToChangeException extends AppBaseException {
    public PasswordToChangeException() {
        super(HttpStatusCode.valueOf(HttpStatus.PRECONDITION_REQUIRED.value()), ChangePasswordConsts.PASSWORD_TO_CHANGE_EXCEPTION_MESSAGE);
    }
}
