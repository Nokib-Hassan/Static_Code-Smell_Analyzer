package com.oopsmellanalyzer.analysis;

import com.oopsmellanalyzer.model.CodeClass;
import com.oopsmellanalyzer.model.CodeMethod;
import java.util.List;

public final class InappropriateIntimacyRule implements CodeSmellRule {
    private static final long MIN_EXTERNAL_FIELD_ACCESSES = 4;

    @Override
    public List<SmellFinding> findIn(CodeClass codeClass, String sourceName) {
        return codeClass.methods().stream()
                .flatMap(method -> method.collaboratorFieldAccesses().entrySet().stream()
                        .filter(entry -> entry.getValue() >= MIN_EXTERNAL_FIELD_ACCESSES)
                        .map(entry -> finding(codeClass, method, sourceName, entry.getKey(), entry.getValue())))
                .toList();
    }

    private SmellFinding finding(CodeClass codeClass, CodeMethod method,
                                 String sourceName, String collaborator, long accessCount) {
        return new SmellFinding("Inappropriate Intimacy", sourceName, method.line(),
                "%s.%s directly reads %d fields from %s (limit: %d).".formatted(
                        codeClass.name(), method.name(), accessCount, collaborator,
                        MIN_EXTERNAL_FIELD_ACCESSES),
                "Hide the collaborator's details behind focused methods.");
    }
}
