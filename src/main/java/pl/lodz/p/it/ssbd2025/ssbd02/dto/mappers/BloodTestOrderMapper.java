package pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodTestOrderDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodTestOrderWithClientDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestOrder;

import java.util.List;

@Mapper(componentModel = "spring", uses = ClientMapper.class)
public interface BloodTestOrderMapper {
    @Mappings({
            @Mapping(target = "clientId", source = "client.id"),
            @Mapping(target = "dieticianId", source = "dietician.id")
    })
    BloodTestOrderDTO toBloodTestOrderDTO(BloodTestOrder bloodTestOrder);
    BloodTestOrder toBloodTestOrder(BloodTestOrderDTO bloodTestOrderDTO);

    @Mappings({
        @Mapping(target = "bloodTestOrderDTO", source = "."),
        @Mapping(target = "minimalClientDTO", source = "client")
    })
    BloodTestOrderWithClientDTO toBloodTestOrderWithClientDTO(BloodTestOrder bloodTestOrder);

    List<BloodTestOrderWithClientDTO> toBloodTestOrderWithClientDTO(List<BloodTestOrder> bloodTestOrders);
}
