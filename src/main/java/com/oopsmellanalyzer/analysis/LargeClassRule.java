package com.oopsmellanalyzer.analysis;

import com.oopsmellanalyzer.model.CodeClass;
import java.util.List;

public final class LargeClassRule implements CodeSmellRule {
    private static final int MAX_FIELDS = 3;

    @Override
    public List<SmellFinding> findIn(CodeClass codeClass, String sourceName) {
        if (codeClass.fieldCount() <= MAX_FIELDS) {
            return List.of();
        }
        return List.of(new SmellFinding("Large Class", sourceName, codeClass.line(),
                "%s has %d fields (limit: %d).".formatted(codeClass.name(), codeClass.fieldCount(),
                        MAX_FIELDS),
                "Check whether related fields and methods belong in smaller classes."));
    }
}
