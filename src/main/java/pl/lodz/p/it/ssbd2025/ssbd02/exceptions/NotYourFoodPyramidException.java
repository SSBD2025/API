package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class NotYourFoodPyramidException extends AppBaseException {
  public NotYourFoodPyramidException() {
    super(HttpStatusCode.valueOf(HttpStatus.FORBIDDEN.value()), ExceptionConsts.NOT_YOUR_PYRAMID);
  }
}
