package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientDetailsDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.MinimalClientDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserRoleMapper.class,AccountMapper.class,PeriodicSurveyMapper.class,SurveyMapper.class, ClientBloodTestReportMapper.class})
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    @Mappings({
            @Mapping(target = "client", source = "client"),
            @Mapping(target = "account", source = "account"),
    })
    ClientDTO toClientDTO(Client client);

    @Mappings({
            @Mapping(target = "firstName", source = "account.firstName"),
            @Mapping(target = "lastName", source = "account.lastName"),
            @Mapping(source = "periodicSurveys", target = "periodicSurvey"),
            @Mapping(source = "survey", target = "survey"),
//    @Mapping(source = "bloodTestReports", target = "bloodTestReport") TODO odkomentowac po merge
    })
    ClientDetailsDTO toDetailsDto(Client client);

    List<ClientDTO> toClientListDTO(List<Client> clients);

    @Mappings({
            @Mapping(target = "firstName", source = "account.firstName"),
            @Mapping(target = "lastName", source = "account.lastName"),
            @Mapping(target = "email", source = "account.email")
    })
    MinimalClientDTO toMinimalClientDTO(Client client);

    @Mappings({
            @Mapping(target = "firstName", source = "account.firstName"),
            @Mapping(target = "lastName", source = "account.lastName"),
            @Mapping(target = "email", source = "account.email")
    })
    List<MinimalClientDTO> toMinimalClientListDTO(List<Client> clients);
}
