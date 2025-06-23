package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

public class FoodPyramidNameAlreadyInUseException extends AppBaseException {
    public FoodPyramidNameAlreadyInUseException(String key) {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), key);
    }
    public FoodPyramidNameAlreadyInUseException() {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), ExceptionConsts.FOOD_PYRAMID_NAME_ALREADY_IN_USE);
    }
    public FoodPyramidNameAlreadyInUseException(Throwable cause) {
        super(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), ExceptionConsts.FOOD_PYRAMID_NAME_ALREADY_IN_USE, cause);
    }
}