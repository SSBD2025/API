package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class RoleAlreadyAssgined extends AppBaseException {
    public RoleAlreadyAssgined() {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), "Role is already assgined");

    }
}
