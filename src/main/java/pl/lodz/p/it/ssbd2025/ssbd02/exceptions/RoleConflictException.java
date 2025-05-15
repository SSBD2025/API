package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class RoleConflictException extends AppBaseException {
    public RoleConflictException() {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), "Account cannot have both DIETICIAN " +
                "and CLIENT roles simultaneously.");

    }
}
