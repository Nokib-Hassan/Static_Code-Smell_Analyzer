package com.oopsmellanalyzer.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record CodeClass(String name, int line, int fieldCount, List<CodeMethod> methods,
                        boolean extendsAnotherClass, Set<String> collaboratorTypes) {
    public CodeClass {
        Objects.requireNonNull(name, "name must not be null");
        methods = List.copyOf(methods);
        collaboratorTypes = Set.copyOf(collaboratorTypes);
    }
}
