package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.PeriodicSurveyDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.PeriodicSurvey;

@Mapper(componentModel = "spring")
public interface PeriodicSurveyMapper {
    PeriodicSurveyMapper INSTANCE = Mappers.getMapper(PeriodicSurveyMapper.class);


    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "client", ignore = true),
            @Mapping(target = "measurementDate", ignore = true)
    })
    public PeriodicSurvey toPeriodicSurvey(PeriodicSurveyDTO periodicSurveyDTO);

    @Mappings({
            @Mapping(target = "clientId", source = "client.id"),
            @Mapping(target = "version", ignore = true),
    })
    public PeriodicSurveyDTO toPeriodicSurveyDTO(PeriodicSurvey periodicSurvey);
}
