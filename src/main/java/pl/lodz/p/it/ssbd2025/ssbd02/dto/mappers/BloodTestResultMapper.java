package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodTestResultDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestResult;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

@Mapper(componentModel = "spring", uses = {BloodParameterMapper.class})
public abstract class BloodTestResultMapper {

    @Autowired
    protected LockTokenService lockTokenService;

    @Mappings({
        @Mapping(target = "id"),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "report", ignore = true),
        @Mapping(target = "bloodParameter", source = "bloodTestResult.bloodParameter", qualifiedByName = "toBloodParameterDTO"),
        @Mapping(target = "result", source = "bloodTestResult.result"),
        @Mapping(target = "lockToken", ignore = true)
    })
    public abstract BloodTestResultDTO toBloodTestResultDTO(BloodTestResult bloodTestResult);

    @Mappings({
        @Mapping(target = "result", ignore = false),
        @Mapping(target = "bloodParameter", ignore = false),
        @Mapping(target = "report", ignore = true)
    })
    public abstract BloodTestResult toNewBloodTestReport(BloodTestResultDTO bloodTestResultDTO);

    @AfterMapping
    protected void injectLockToken(BloodTestResult result, @MappingTarget BloodTestResultDTO dto) {
        dto.setLockToken(fetchLockToken(result));
    }

    protected String fetchLockToken(BloodTestResult result) {
        return lockTokenService.generateToken(result.getId(), result.getVersion()).getValue();
    }
}
