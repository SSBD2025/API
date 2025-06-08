package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodParameterDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.BloodParameter;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.InvalidBloodParameterException;


public interface BloodParameterMapper {

    @Named("toBloodParameterDTO")
    BloodParameterDTO toBloodParameterDTO(BloodParameter parameter, boolean isMan);

    BloodParameter toBloodParameter(BloodParameterDTO bloodParameterDTO);

}
