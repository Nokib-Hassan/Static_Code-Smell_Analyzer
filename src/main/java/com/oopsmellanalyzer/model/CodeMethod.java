package com.oopsmellanalyzer.model;

import java.util.Map;
import java.util.Objects;

public record CodeMethod(String name, int line, int lineCount, int parameterCount,
                         Map<String, Long> collaboratorCalls, Map<String, Long> collaboratorFieldAccesses,
                         boolean overridesMethod, boolean emptyBody, boolean rejectsInheritedBehavior) {
    public CodeMethod {
        Objects.requireNonNull(name, "name must not be null");
        collaboratorCalls = Map.copyOf(collaboratorCalls);
        collaboratorFieldAccesses = Map.copyOf(collaboratorFieldAccesses);
    }
}
