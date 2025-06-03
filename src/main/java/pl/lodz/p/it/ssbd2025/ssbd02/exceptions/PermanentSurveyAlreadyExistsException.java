package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class PermanentSurveyAlreadyExistsException extends AppBaseException {
    public PermanentSurveyAlreadyExistsException() {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), ExceptionConsts.PERMANENT_SURVEY_ALREADY_EXISTS);
    }
}
