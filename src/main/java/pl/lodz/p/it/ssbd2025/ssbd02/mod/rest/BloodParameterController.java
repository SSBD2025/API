package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodParameterDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.BloodParameter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@EnableMethodSecurity(prePostEnabled = true)
@RequestMapping("/api/mod/blood-parameters")
public class BloodParameterController {

    @PreAuthorize("hasRole('DIETICIAN')")
    @GetMapping("/{male}")
    public List<BloodParameterDTO> getAllBloodParameters(@PathVariable Boolean male) {
        return Arrays.stream(BloodParameter.values())
                .map(param -> new BloodParameterDTO(
                        param.name(),
                        param.getDescription(),
                        param.getUnit().toString(),
                        male ? param.getMenStandardMin() : param.getWomanStandardMin(),
                        male ? param.getMenStandardMax() : param.getWomanStandardMax()
                ))
                .collect(Collectors.toList());
    }
}
