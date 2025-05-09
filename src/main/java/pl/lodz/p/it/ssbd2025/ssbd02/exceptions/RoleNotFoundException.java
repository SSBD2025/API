package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class RoleNotFoundException extends AppBaseException{
    public RoleNotFoundException() {
        super(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), "Role not found");
    }
}
