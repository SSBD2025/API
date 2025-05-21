package pl.lodz.p.it.ssbd2025.ssbd02.mod.services.interfaces;

import pl.lodz.p.it.ssbd2025.ssbd02.entities.DietaryRestrictions;

import java.util.UUID;

public interface IDietaryRestrictionsService {
    DietaryRestrictions getByClientId(UUID clientId);
    DietaryRestrictions updateRestrictions(UUID clientId, DietaryRestrictions restrictions);
}
