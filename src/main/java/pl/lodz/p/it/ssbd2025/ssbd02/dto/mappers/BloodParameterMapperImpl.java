package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodParameterDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.BloodParameter;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.InvalidBloodParameterException;

@Component
public class BloodParameterMapperImpl implements BloodParameterMapper {

    @Override
    public BloodParameter toBloodParameter(BloodParameterDTO dto) {
        if (dto == null || dto.getName() == null) {
            throw new InvalidBloodParameterException();
        }
        try {
            return BloodParameter.valueOf(dto.getName());
        } catch (IllegalArgumentException e) {
            throw new InvalidBloodParameterException();
        }
    }

    @Named("toBloodParameterDTO")
    @Override
    public BloodParameterDTO toBloodParameterDTO(BloodParameter parameter) {
        return new BloodParameterDTO(
                parameter.name(),
                parameter.getDescription(),
                parameter.getUnit().toString(),
                parameter.getWomanStandardMin(),
                parameter.getWomanStandardMax(),
                parameter.getMenStandardMin(),
                parameter.getMenStandardMax()
        );
    }
}

