package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.UserRoleDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Admin;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Dietician;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.UserRole;

@Mapper(componentModel = "spring")
public interface UserRoleMapper {
    UserRoleMapper INSTANCE = Mappers.getMapper(UserRoleMapper.class);

    @Mappings({
            @Mapping(target = "roleName", ignore = true),
            @Mapping(target = "active", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    UserRoleDTO.AdminDTO toAdminDTO(Admin admin);

    @Mappings({
            @Mapping(target = "roleName", ignore = true),
            @Mapping(target = "active", ignore = true),
    })
    UserRoleDTO.DieticianDTO toDieticianDTO(Dietician dietician);

    @Mappings({
            @Mapping(target = "roleName", ignore = true),
            @Mapping(target = "active", ignore = true),
    })
    UserRoleDTO.ClientDTO toClientDTO(Client client);

    @Mappings({
            @Mapping(target = "roleName", ignore = true),
            @Mapping(target = "active", ignore = true),
    })
    @SubclassMappings({
            @SubclassMapping(target = UserRoleDTO.AdminDTO.class, source = Admin.class),
            @SubclassMapping(target = UserRoleDTO.DieticianDTO.class, source = Dietician.class),
            @SubclassMapping(target = UserRoleDTO.ClientDTO.class, source = Client.class)
    })
    UserRoleDTO toUserRoleDTO(UserRole userRole);

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
//            @Mapping(target = "nip"), ??
//            @Mapping(target = "survey")
    })
    public Client toNewClientData(UserRoleDTO.ClientDTO clientDataDTO);

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
//            @Mapping(target = "alarmCode"), ??
    })
    public Admin toNewAdminData(UserRoleDTO.AdminDTO adminDataDTO);

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
//            @Mapping(target = "intercom"), ??
    })
    public Dietician toNewDietician(UserRoleDTO.DieticianDTO DieticianDTO);
}
