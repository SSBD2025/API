package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodParameterDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.BloodParameter;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.InvalidBloodParameterException;

//@Mapper(componentModel = "spring")
public interface BloodParameterMapper {
//    BloodParameterMapper INSTANCE = Mappers.getMapper(BloodParameterMapper.class);

//    default BloodParameterDTO toBloodParameterDTO(BloodParameter parameter, boolean isMan) {
//        return new BloodParameterDTO(parameter, isMan);
//    }
//
//    default BloodParameter toBloodParameter(BloodParameterDTO bloodParameterDTO) {
//        if (bloodParameterDTO == null || bloodParameterDTO.getName() == null) {
//            throw new InvalidBloodParameterException();
//        }
//        try {
//            return BloodParameter.valueOf(bloodParameterDTO.getName());
//        } catch (IllegalArgumentException e) {
//            throw new InvalidBloodParameterException();
//        }
//    }
    @Named("toBloodParameterDTO")
    BloodParameterDTO toBloodParameterDTO(BloodParameter parameter);

    BloodParameter toBloodParameter(BloodParameterDTO bloodParameterDTO);

//    default BloodParameterDTO toBloodParameterDTO(BloodParameter parameter) {
//        return new BloodParameterDTO(parameter.name(), parameter.getDescription(), parameter.getUnit().toString(), parameter.getWomanStandardMin(), parameter.getWomanStandardMax(), parameter.getMenStandardMin(), parameter.getMenStandardMax());
//    }
//
//    default BloodParameter toBloodParameter(BloodParameterDTO bloodParameterDTO) {
//        if (bloodParameterDTO == null || bloodParameterDTO.getName() == null) {
//            throw new InvalidBloodParameterException();
//        }
//        try {
//            return BloodParameter.valueOf(bloodParameterDTO.getName());
//        } catch (IllegalArgumentException e) {
//            throw new InvalidBloodParameterException();
//        }
//    }
}
