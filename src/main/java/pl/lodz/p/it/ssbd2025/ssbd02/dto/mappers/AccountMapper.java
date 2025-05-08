package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountReadDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountWithRolesDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;

@Mapper(componentModel = "spring", uses = UserRoleMapper.class)
public interface AccountMapper {

    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    @Mappings({
            @Mapping(target = "password", ignore = true, defaultValue = "***"),
    })
    AccountDTO toAccountDTO(Account account);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "verified", ignore = true),
            @Mapping(target = "active", ignore = true),
            @Mapping(target = "lastSuccessfulLogin", ignore = true),
            @Mapping(target = "lastFailedLogin", ignore = true),
            @Mapping(target = "language", ignore = false),
            @Mapping(target = "lastSuccessfulLoginIp", ignore = true),
            @Mapping(target = "lastFailedLoginIp", ignore = true),
            @Mapping(target = "userRoles", ignore = true),
    })
    Account toNewAccount(AccountDTO accountDTO);

    @Mappings({
            @Mapping(target = "accountDTO", source = "account"),
            @Mapping(target = "userRoleDTOS", source = "account.userRoles"),
    })
    AccountWithRolesDTO toAccountWithUserRolesDTO(Account account);

    @Mappings({
            @Mapping(target = "userRoles", ignore = true),
    })
    Account toUpdateAccount(AccountDTO accountDTO);

    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "firstName"),
            @Mapping(target = "lastName"),
            @Mapping(target = "email"),
    })
    void updateExistingAccount(Account accountUpdate, @MappingTarget Account account);

    AccountReadDTO toReadDTO(Account account);

}

