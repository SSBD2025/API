package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.Mapper;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FoodPyramidDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FoodPyramidMapper {
    FoodPyramidDTO toDto(FoodPyramid foodPyramid);
    List<FoodPyramidDTO> toDtoList(List<FoodPyramid> foodPyramids);
}
