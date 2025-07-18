package pl.lodz.p.it.ssbd2025.ssbd02.interceptors;

import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {
    @Pointcut(
            "execution(public * org.springframework.data.repository.Repository+.*(..))"
    )
    public void allRepositoryMethods() {}

    @Pointcut(
            "execution(@pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged * *(..)) " +
                    "|| within(@pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged *)"
    )
    public void methodCallLoggedAnnotatedMethods() {}

    @Pointcut(
            "execution(@pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged * *(..)) " +
                    "|| within(@pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged *)"
    )
    public void transactionLoggedAnnotatedMethods() {}

    @Pointcut(
            "execution(* pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations.AccountService.login(..))"
    )
    public void loginMethod() {}

    @Pointcut(
            "execution(@pl.lodz.p.it.ssbd2025.ssbd02.interceptors.RoleChangeLogged * *(..)) " +
                    "|| within(@pl.lodz.p.it.ssbd2025.ssbd02.interceptors.RoleChangeLogged *)"
    )
    public void roleChangeLoggedAnnotatedMethods() {}

    @Pointcut(
            "execution(@pl.lodz.p.it.ssbd2025.ssbd02.interceptors.UserRoleChangeLogged * *(..)) " +
                    "|| within(@pl.lodz.p.it.ssbd2025.ssbd02.interceptors.UserRoleChangeLogged *)"
    )
    public void userRoleChangeLoggedAnnotatedMethods() {}

    @Pointcut(
            "execution(@pl.lodz.p.it.ssbd2025.ssbd02.interceptors.RollbackUnknown * *(..)) " +
                    "|| within(@pl.lodz.p.it.ssbd2025.ssbd02.interceptors.RollbackUnknown *)"
    )
    public void transactionRollbackOnUnknown() {}
}
