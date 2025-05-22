package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestResult;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IBloodTestResultService;

import java.util.List;
import java.util.UUID;

public class BloodTestResultService implements IBloodTestResultService {
    @Override
    public List<BloodTestResult> getAllByReportId(UUID reportId) {
        return List.of();
    }

    @Override
    public BloodTestResult getById(UUID id) {
        return null;
    }

    @Override
    public BloodTestResult addBloodTestResult(UUID reportId, BloodTestResult result) {
        return null;
    }

    @Override
    public void deleteBloodTestResult(UUID id) {

    }
}
