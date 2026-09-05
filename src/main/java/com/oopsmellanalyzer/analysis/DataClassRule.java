package com.oopsmellanalyzer.analysis;

import com.oopsmellanalyzer.model.CodeClass;
import com.oopsmellanalyzer.model.CodeMethod;
import java.util.List;
import java.util.Locale;

public final class DataClassRule implements CodeSmellRule {
    private static final int MIN_FIELDS = 3;

    @Override
    public List<SmellFinding> findIn(CodeClass codeClass, String sourceName) {
        if (codeClass.fieldCount() < MIN_FIELDS || codeClass.methods().isEmpty()
                || !codeClass.methods().stream().allMatch(this::isAccessorOrMutator)) {
            return List.of();
        }
        return List.of(new SmellFinding("Data Class", sourceName, codeClass.line(),
                "%s has %d fields and only accessor or mutator methods.".formatted(
                        codeClass.name(), codeClass.fieldCount()),
                "Move behavior related to this data into the class where it belongs."));
    }

    private boolean isAccessorOrMutator(CodeMethod method) {
        String name = method.name().toLowerCase(Locale.ROOT);
        boolean accessor = (name.startsWith("get") || name.startsWith("is"))
                && method.parameterCount() == 0;
        boolean mutator = name.startsWith("set") && method.parameterCount() == 1;
        return accessor || mutator;
    }
}
