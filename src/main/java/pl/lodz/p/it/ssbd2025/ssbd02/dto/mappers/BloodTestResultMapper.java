package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodTestResultDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestResult;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

@Mapper(componentModel = "spring")
public abstract class BloodTestResultMapper {

    @Autowired
    protected LockTokenService lockTokenService;

    @Autowired
    protected BloodParameterMapper bloodParameterMapper;

    @Mappings({
            @Mapping(target = "id"),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "report", ignore = true),
            @Mapping(target = "bloodParameter", ignore = true),
            @Mapping(target = "result", source = "bloodTestResult.result"),
            @Mapping(target = "lockToken", ignore = true)
    })
    public abstract BloodTestResultDTO toBloodTestResultDTO(BloodTestResult bloodTestResult, boolean isMan);

    @AfterMapping
    protected void mapBloodParameter(BloodTestResult source, @MappingTarget BloodTestResultDTO target, boolean isMan) {
        if (source.getBloodParameter() != null) {
            target.setBloodParameter(bloodParameterMapper.toBloodParameterDTO(source.getBloodParameter(), isMan));
        }
    }

    @Mappings({
            @Mapping(target = "result", source = "result"),
            @Mapping(target = "bloodParameter", ignore = true),
            @Mapping(target = "report", ignore = true)
    })
    public abstract BloodTestResult toNewBloodTestReport(BloodTestResultDTO bloodTestResultDTO);

    @AfterMapping
    protected void mapBloodParameterFromDTO(BloodTestResultDTO source, @MappingTarget BloodTestResult target) {
        if (source.getBloodParameter() != null) {
            target.setBloodParameter(bloodParameterMapper.toBloodParameter(source.getBloodParameter()));
        }
    }

    @AfterMapping
    protected void injectLockToken(BloodTestResult result, @MappingTarget BloodTestResultDTO dto) {
        dto.setLockToken(fetchLockToken(result));
    }

    protected String fetchLockToken(BloodTestResult result) {
        return lockTokenService.generateToken(result.getId(), result.getVersion()).getValue();
    }
}