package com.oopsmellanalyzer.analysis;

import com.oopsmellanalyzer.model.CodeClass;
import com.oopsmellanalyzer.model.CodeMethod;
import java.util.List;

public final class FeatureEnvyRule implements CodeSmellRule {
    private static final long MIN_EXTERNAL_CALLS = 4;

    @Override
    public List<SmellFinding> findIn(CodeClass codeClass, String sourceName) {
        return codeClass.methods().stream()
                .flatMap(method -> method.collaboratorCalls().entrySet().stream()
                        .filter(entry -> entry.getValue() >= MIN_EXTERNAL_CALLS)
                        .map(entry -> finding(codeClass, method, sourceName, entry.getKey(), entry.getValue())))
                .toList();
    }

    private SmellFinding finding(CodeClass codeClass, CodeMethod method,
                                 String sourceName, String collaborator, long callCount) {
        return new SmellFinding("Feature Envy", sourceName, method.line(),
                "%s.%s makes %d calls to %s (limit: %d).".formatted(
                        codeClass.name(), method.name(), callCount, collaborator,
                        MIN_EXTERNAL_CALLS),
                "Consider moving this behavior to " + collaborator + " or extracting a shared service.");
    }
}
