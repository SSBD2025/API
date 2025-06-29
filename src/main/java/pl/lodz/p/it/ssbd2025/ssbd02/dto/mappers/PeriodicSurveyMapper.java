package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.PeriodicSurveyDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.PeriodicSurvey;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

@Mapper(componentModel = "spring")
public abstract class PeriodicSurveyMapper {

    @Autowired
    protected LockTokenService lockTokenService;

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "client", ignore = true),
            @Mapping(target = "measurementDate", ignore = true)
    })
    public abstract PeriodicSurvey toPeriodicSurvey(PeriodicSurveyDTO periodicSurveyDTO);

    @Mappings({
            @Mapping(target = "clientId", source = "client.id"),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "createdAt", source = "periodicSurvey.createdAt"),
            @Mapping(target = "lockToken", ignore = true)
    })
    public abstract PeriodicSurveyDTO toPeriodicSurveyDTO(PeriodicSurvey periodicSurvey);

    @AfterMapping
    protected void injectLockToken(PeriodicSurvey source, @MappingTarget PeriodicSurveyDTO target) {
        if (source.getId() != null && source.getVersion() != null) {
            String token = lockTokenService.generateToken(source.getId(), source.getVersion()).getValue();
            target.setLockToken(token);
        }
    }
}
