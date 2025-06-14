package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.DieticianDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserRoleMapper.class,AccountMapper.class})
public interface DieticianMapper {

    DieticianMapper INSTANCE = Mappers.getMapper(DieticianMapper.class);

    @Mappings({
            @Mapping(target = "dietician", source = "dietician"),
            @Mapping(target = "account.id", ignore = true),
            @Mapping(target = "account.version", ignore = true),
            @Mapping(target = "account.verified", ignore = true),
            @Mapping(target = "account.login", ignore = true),
            @Mapping(target = "account.active", ignore = true),
            @Mapping(target = "account.lastSuccessfulLogin", ignore = true),
            @Mapping(target = "account.lastFailedLogin", ignore = true),
            @Mapping(target = "account.language", ignore = true),
            @Mapping(target = "account.lastSuccessfulLoginIp", ignore = true),
            @Mapping(target = "account.lastFailedLoginIp", ignore = true),
            @Mapping(target = "account.reminded", ignore = true),
            @Mapping(target = "account.loginAttempts", ignore = true),
            @Mapping(target = "account.lockedUntil", ignore = true),
            @Mapping(target = "account.password", ignore = true),
            @Mapping(target = "account.twoFactorAuth", ignore = true),
            @Mapping(target = "account.autoLocked", ignore = true),
    })
    DieticianDTO toDieticianDTO(Dietician dietician);

    List<DieticianDTO> toDieticianListDTO(List<Dietician> dieticianList);
}

