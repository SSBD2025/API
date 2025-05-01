package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AdminDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Admin;

@Mapper(componentModel = "spring", uses = {UserRoleMapper.class,AccountMapper.class})
public interface AdminMapper {

    AdminMapper INSTANCE = Mappers.getMapper(AdminMapper.class);

    @Mappings({
            @Mapping(target = "admin", source = "admin"),
            @Mapping(target = "account", source = "account"),
    })
    AdminDTO toAdminDTO(Admin admin);
}
