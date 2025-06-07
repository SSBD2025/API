package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodTestResultDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientBloodTestReportDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestResult;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientBloodTestReport;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

@Mapper(componentModel = "spring", uses = {BloodTestResultMapper.class, BloodParameterMapper.class})
public abstract class ClientBloodTestReportMapper {

    @Autowired
    protected LockTokenService lockTokenService;

//    ClientBloodTestReportMapper INSTANCE = Mappers.getMapper(ClientBloodTestReportMapper.class);

    @Mappings({
        @Mapping(target = "id"),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "client", ignore = true),
        @Mapping(target = "timestamp"),
        @Mapping(target = "results"),
        @Mapping(target = "lockToken", ignore = true),
    })
    public abstract ClientBloodTestReportDTO toClientBloodTestReportDTO(ClientBloodTestReport clientBloodTestReport);

    @Mappings({
            @Mapping(target = "client", ignore = true),
            @Mapping(target = "timestamp", ignore = false),
            @Mapping(target = "results", ignore = false),
    })
    public abstract ClientBloodTestReport toNewClientBloodTestReport(ClientBloodTestReportDTO clientBloodTestReportDTO);

    @AfterMapping
    protected void injectLockToken(BloodTestResult result, @MappingTarget BloodTestResultDTO dto) {
        dto.setLockToken(fetchLockToken(result));
    }

    protected String fetchLockToken(BloodTestResult result) {
        return lockTokenService.generateToken(result.getId(), result.getVersion()).getValue();
    }
}
