package com.oopsmellanalyzer.analysis;

import java.util.Objects;

public record SmellFinding(String smellName, String sourceName, int line, String message,
                           String suggestion) {
    public SmellFinding {
        Objects.requireNonNull(smellName, "smellName must not be null");
        Objects.requireNonNull(sourceName, "sourceName must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(suggestion, "suggestion must not be null");
    }
}
