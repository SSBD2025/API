package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SurveyDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;

@Mapper(componentModel = "spring")
public interface SurveyMapper {
    SurveyMapper INSTANCE = Mappers.getMapper(SurveyMapper.class);

    @Mappings({
            @Mapping(target = "clientId", source = "client.id")
    })
    SurveyDTO toSurveyDTO(Survey survey);

    Survey toSurvey(SurveyDTO surveyDTO);
}
