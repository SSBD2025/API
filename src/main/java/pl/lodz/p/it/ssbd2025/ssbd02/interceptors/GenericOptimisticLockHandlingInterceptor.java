package pl.lodz.p.it.ssbd2025.ssbd02.interceptors;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ConcurrentUpdateException;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@Component
public class GenericOptimisticLockHandlingInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GenericOptimisticLockHandlingInterceptor.class);

    @AfterThrowing(pointcut = "Pointcuts.allRepositoryMethods()", throwing = "olfe")
    public void handleOptimisticLockException(JoinPoint joinPoint, OptimisticLockingFailureException olfe) {
        throw new ConcurrentUpdateException(olfe);
    }

}