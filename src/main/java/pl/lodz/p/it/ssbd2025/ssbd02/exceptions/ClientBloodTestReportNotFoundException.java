package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class ClientBloodTestReportNotFoundException extends AppBaseException {
    public ClientBloodTestReportNotFoundException() {
        super(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), ExceptionConsts.CLIENT_BLOOD_TEST_REPORT_NOT_FOUND);
    }
}
