package pl.lodz.p.it.ssbd2025.ssbd02.interceptors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@Component
public class TransactionUnknownInterceptor {
    private static final Logger log = LoggerFactory.getLogger("TransactionUnknown");

    @Around("Pointcuts.transactionRollbackOnUnknown()")
    public Object logTransactionUnknown(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        TransactionSynchronizationLogger logger = new TransactionSynchronizationLogger();
        TransactionSynchronizationLogger.threadLocalTSLogger.set(logger);

        Object result = proceedingJoinPoint.proceed();

        if (!logger.isCommitted()) {
            throw new TransactionSynchronizationLogger.TransactionNotCommittedException();
        }

        return result;
    }
}