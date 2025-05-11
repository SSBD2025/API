package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class SelfRoleAssignmentException extends AppBaseException {
    public SelfRoleAssignmentException() {
      super(HttpStatusCode.valueOf(HttpStatus.FORBIDDEN.value()), "Self-role assignment is not allowed.");
    }
}
