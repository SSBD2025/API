package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class SameDieticianAlreadyAssignedException extends AppBaseException {
    public SameDieticianAlreadyAssignedException() {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), ExceptionConsts.SAME_DIETICIAN_ALREADY_ASSIGNED);
    }
}
