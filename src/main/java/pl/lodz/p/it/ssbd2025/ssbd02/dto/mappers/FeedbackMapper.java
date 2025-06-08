package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FeedbackDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Feedback;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {
    FeedbackMapper INSTANCE = Mappers.getMapper(FeedbackMapper.class);

    @Mappings({
            @Mapping(target = "clientId", source = "client.id"),
            @Mapping(target = "foodPyramidId", source = "foodPyramid.id")
    })
    FeedbackDTO toFeedbackDTO(Feedback feedback);

    Feedback toFeedback(FeedbackDTO feedbackDTO);
}
