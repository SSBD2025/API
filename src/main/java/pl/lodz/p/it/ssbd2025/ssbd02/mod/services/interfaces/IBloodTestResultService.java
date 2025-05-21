package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestResult;

import java.util.List;
import java.util.UUID;

public interface IBloodTestResultService {
    List<BloodTestResult> getAllByReportId(UUID reportId);
    BloodTestResult getById(UUID id);
    BloodTestResult addBloodTestResult(UUID reportId, BloodTestResult result);
    void deleteBloodTestResult(UUID id);
}
