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
            @Mapping(target = "account", source = "account"),
    })
    DieticianDTO toDieticianDTO(Dietician dietician);

    List<DieticianDTO> toDieticianListDTO(List<Dietician> dieticianList);
}

