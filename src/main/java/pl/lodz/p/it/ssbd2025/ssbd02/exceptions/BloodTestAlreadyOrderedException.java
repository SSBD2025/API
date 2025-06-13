package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class BloodTestAlreadyOrderedException extends AppBaseException {
    public BloodTestAlreadyOrderedException() {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), ExceptionConsts.BLOOD_TEST_ALREADY_ORDERED);
    }
}
