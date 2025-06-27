package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class PaymentException extends AppBaseException {
    public PaymentException() {
        super(HttpStatusCode.valueOf(HttpStatus.BAD_REQUEST.value()), ExceptionConsts.PAYMENT_STRIPE_EXCEPTION);
    }
}