package com.oopsmellanalyzer.analysis;

import com.oopsmellanalyzer.model.CodeClass;
import com.oopsmellanalyzer.model.CodeMethod;
import java.util.List;

public final class RefusedBequestRule implements CodeSmellRule {
    @Override
    public List<SmellFinding> findIn(CodeClass codeClass, String sourceName) {
        if (!codeClass.extendsAnotherClass()) {
            return List.of();
        }
        return codeClass.methods().stream()
                .filter(this::isUnsupportedOverride)
                .map(method -> finding(codeClass, method, sourceName))
                .toList();
    }

    private boolean isUnsupportedOverride(CodeMethod method) {
        return method.overridesMethod() && (method.emptyBody() || method.rejectsInheritedBehavior());
    }

    private SmellFinding finding(CodeClass codeClass, CodeMethod method,
                                 String sourceName) {
        return new SmellFinding("Refused Bequest", sourceName, method.line(),
                "%s overrides %s but leaves it empty or rejects it.".formatted(
                        codeClass.name(), method.name()),
                "Use composition or a narrower parent type if this behavior does not belong here.");
    }
}
