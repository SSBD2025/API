package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class ClientHasNoAssignedDieticianException extends AppBaseException {
    public ClientHasNoAssignedDieticianException() {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), ExceptionConsts.CLIENT_HAS_NO_ASSIGNED_DIETICIAN);
    }
}
