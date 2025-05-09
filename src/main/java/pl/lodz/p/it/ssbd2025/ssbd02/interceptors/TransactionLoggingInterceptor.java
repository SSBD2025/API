package pl.lodz.p.it.ssbd2025.ssbd02.interceptors;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Aspect @Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class TransactionLoggingInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TransactionLoggingInterceptor.class);

    @Before("Pointcuts.transactionLoggedAnnotatedMethods()")
    public void registerSynchronization() throws Throwable{

        final String transactionId = TransactionSynchronizationManager.getCurrentTransactionName() + ":" + String.valueOf(Thread.currentThread().threadId())
        + (null != RetrySynchronizationManager.getContext()?" (retry # "+RetrySynchronizationManager.getContext().getRetryCount()+")":"");

        TransactionSynchronizationLogger synchronizationLogger = new TransactionSynchronizationLogger(transactionId);
        TransactionSynchronizationManager.registerSynchronization(synchronizationLogger);
        log.trace("Transaction synchronization: {} registered", synchronizationLogger.getTransactionId());
    }

}