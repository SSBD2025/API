package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserRoleMapper.class,AccountMapper.class})
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    @Mappings({
            @Mapping(target = "client", source = "client"),
            @Mapping(target = "account", source = "account"),
    })
    ClientDTO toClientDTO(Client client);

    List<ClientDTO> toClientListDTO(List<Client> clients);
}
