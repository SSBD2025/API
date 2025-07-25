package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class SelfRoleAssignmentException extends AppBaseException {
    public SelfRoleAssignmentException() {
      super(HttpStatusCode.valueOf(HttpStatus.FORBIDDEN.value()), ExceptionConsts.SELF_ROLE_ASSIGMENT);
    }
}
