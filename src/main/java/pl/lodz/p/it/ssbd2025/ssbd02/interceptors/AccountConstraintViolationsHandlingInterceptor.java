package pl.lodz.p.it.ssbd2025.ssbd02.interceptors;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AccountConstraintViolationException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.FoodPyramidNameAlreadyInUseException;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

@Aspect @Order(Ordered.LOWEST_PRECEDENCE-100) // So that it's "external" compared to logging interceptor
@Component
public class AccountConstraintViolationsHandlingInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AccountConstraintViolationsHandlingInterceptor.class);

    @AfterThrowing(pointcut = "Pointcuts.allRepositoryMethods()", throwing = "dive")
    public void handleDataIntegrityViolationException(DataIntegrityViolationException dive) {
        if(dive.getMessage().contains("account_login_key"))
            throw new AccountConstraintViolationException(ExceptionConsts.ACCOUNT_CONSTRAINT_VIOLATION + ": login already in use");
        else if(dive.getMessage().contains("account_email_key"))
            throw new AccountConstraintViolationException(ExceptionConsts.ACCOUNT_CONSTRAINT_VIOLATION + ": email already in use");
        else if(dive.getMessage().contains("food_pyramid_name_key")) {
            throw new FoodPyramidNameAlreadyInUseException(ExceptionConsts.FOOD_PYRAMID_NAME_ALREADY_IN_USE);
        }
        else
            throw new AccountConstraintViolationException(dive);
    }
}
