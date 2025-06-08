package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class FeedbackNotFoundException extends AppBaseException  {
  public FeedbackNotFoundException() {
    super(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), ExceptionConsts.FEEDBACK_NOT_FOUND);
  }
}
