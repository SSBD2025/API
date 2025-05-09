package pl.lodz.p.it.ssbd2025.ssbd02.interceptors;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AccountConstraintViolationException;

@Aspect @Order(Ordered.LOWEST_PRECEDENCE-100) // So that it's "external" compared to logging interceptor
@Component
public class AccountConstraintViolationsHandlingInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AccountConstraintViolationsHandlingInterceptor.class);

    @AfterThrowing(pointcut = "Pointcuts.allRepositoryMethods()", throwing = "dive")
    public void handleDataIntegrityViolationException(DataIntegrityViolationException dive) {
//        // Rewrite this to switch pattern matching if you can :) //TODO INDEXES ON ACCOUNT
//        // Find better way to dig the detailed reason if you can :)
//        if(dive.getMessage().contains(Account.UNIQUE_LOGIN_INDEX_NAME))
//            throw new AccountConstraintViolationException(Account.UNIQUE_LOGIN_INDEX_NAME);
//        else if(dive.getMessage().contains(Account.UNIQUE_EMAIL_INDEX_NAME))
//            throw new AccountConstraintViolationException(Account.UNIQUE_EMAIL_INDEX_NAME);
//        else
//            throw new AccountConstraintViolationException(dive);
    }

}
