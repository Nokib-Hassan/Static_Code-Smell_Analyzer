package com.oopsmellanalyzer.analysis;

import com.oopsmellanalyzer.model.CodeClass;
import com.oopsmellanalyzer.model.CodeMethod;
import java.util.ArrayList;
import java.util.List;

public final class LongMethodRule implements CodeSmellRule {
    private static final int MAX_METHOD_LINES = 10;

    @Override
    public List<SmellFinding> findIn(CodeClass codeClass, String sourceName) {
        List<SmellFinding> findings = new ArrayList<>();
        for (CodeMethod method : codeClass.methods()) {
            if (method.lineCount() > MAX_METHOD_LINES) {
                findings.add(new SmellFinding("Long Method", sourceName, method.line(),
                        "%s.%s has %d lines (limit: %d).".formatted(codeClass.name(), method.name(),
                                method.lineCount(), MAX_METHOD_LINES),
                        "Split this method into smaller methods with clear names."));
            }
        }
        return List.copyOf(findings);
    }
}
