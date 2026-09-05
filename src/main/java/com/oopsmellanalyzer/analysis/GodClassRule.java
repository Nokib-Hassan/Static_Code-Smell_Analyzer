package com.oopsmellanalyzer.analysis;

import com.oopsmellanalyzer.model.CodeClass;
import java.util.List;

public final class GodClassRule implements CodeSmellRule {
    private static final int MIN_FIELDS = 5;
    private static final int MIN_METHODS = 10;

    @Override
    public List<SmellFinding> findIn(CodeClass codeClass, String sourceName) {
        if (codeClass.fieldCount() < MIN_FIELDS || codeClass.methods().size() < MIN_METHODS) {
            return List.of();
        }
        return List.of(new SmellFinding("God Class", sourceName, codeClass.line(),
                "%s has %d fields and %d methods (limits: %d fields, %d methods).".formatted(
                        codeClass.name(), codeClass.fieldCount(), codeClass.methods().size(), MIN_FIELDS, MIN_METHODS),
                "Split responsibilities into smaller, focused classes."));
    }
}
