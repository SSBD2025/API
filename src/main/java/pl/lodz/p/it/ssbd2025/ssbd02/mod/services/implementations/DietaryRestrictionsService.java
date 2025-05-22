package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.DietaryRestrictions;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces.IDietaryRestrictionsService;

import java.util.UUID;

public class DietaryRestrictionsService implements IDietaryRestrictionsService {
    @Override
    public DietaryRestrictions getByClientId(UUID clientId) {
        return null;
    }

    @Override
    public DietaryRestrictions updateRestrictions(UUID clientId, DietaryRestrictions restrictions) {
        return null;
    }
}
