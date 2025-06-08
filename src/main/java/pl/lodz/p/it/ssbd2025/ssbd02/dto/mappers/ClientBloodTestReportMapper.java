package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodTestResultDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientBloodTestReportDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestResult;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientBloodTestReport;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ClientBloodTestReportMapper {

    @Autowired
    protected LockTokenService lockTokenService;

    @Autowired
    protected BloodTestResultMapper bloodTestResultMapper;

    @Mappings({
            @Mapping(target = "id"),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "client", ignore = true),
            @Mapping(target = "timestamp"),
            @Mapping(target = "results", ignore = true),
            @Mapping(target = "lockToken", ignore = true),
    })
    public abstract ClientBloodTestReportDTO toClientBloodTestReportDTO(ClientBloodTestReport clientBloodTestReport, boolean isMan);

    @AfterMapping
    protected void mapResults(ClientBloodTestReport source, @MappingTarget ClientBloodTestReportDTO target, boolean isMan) {
        if (source.getResults() != null) {
            List<BloodTestResultDTO> resultDTOs = source.getResults().stream()
                    .map(result -> bloodTestResultMapper.toBloodTestResultDTO(result, isMan))
                    .collect(Collectors.toList());
            target.setResults(resultDTOs);
        }
    }

    @Mappings({
            @Mapping(target = "client", ignore = true),
            @Mapping(target = "timestamp", source = "timestamp"),
            @Mapping(target = "results", ignore = true),
    })
    public abstract ClientBloodTestReport toNewClientBloodTestReport(ClientBloodTestReportDTO clientBloodTestReportDTO);

    @AfterMapping
    protected void mapResultsFromDTO(ClientBloodTestReportDTO source, @MappingTarget ClientBloodTestReport target) {
        if (source.getResults() != null) {
            List<BloodTestResult> results = source.getResults().stream()
                    .map(resultDTO -> bloodTestResultMapper.toNewBloodTestReport(resultDTO))
                    .collect(Collectors.toList());
            target.setResults(results);
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