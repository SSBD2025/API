package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class PermanentSurveyNotFoundException extends AppBaseException {
    public PermanentSurveyNotFoundException() {
        super(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), ExceptionConsts.PERMANENT_SURVEY_NOT_FOUND);
    }
}