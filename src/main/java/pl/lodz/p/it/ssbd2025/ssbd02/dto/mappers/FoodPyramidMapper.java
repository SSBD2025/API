package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import jakarta.persistence.Version;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FoodPyramidDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FoodPyramidMapper {
    FoodPyramidMapper INSTANCE = Mappers.getMapper(FoodPyramidMapper.class);

    FoodPyramidDTO toDto(FoodPyramid foodPyramid);
    List<FoodPyramidDTO> toDtoList(List<FoodPyramid> foodPyramids);

    @Mappings({
        @Mapping(target = "id" , ignore = true),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "averageRating", ignore = true),
        @Mapping(target = "clientFoodPyramids", ignore = true),
        @Mapping(target = "feedbacks", ignore = true),
    })
    FoodPyramid toEntity(FoodPyramidDTO foodPyramidDTO);
}
