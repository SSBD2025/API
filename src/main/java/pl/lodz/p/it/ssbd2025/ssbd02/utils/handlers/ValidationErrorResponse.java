package pl.lodz.p.it.ssbd2025.ssbd02.utils.handlers;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ValidationErrorResponse {
    public record Violation(String fieldName, String message) { }
    private final List<Violation> violations = new ArrayList<>();
}

