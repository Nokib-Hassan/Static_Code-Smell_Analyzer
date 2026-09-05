package com.oopsmellanalyzer.analysis;

import com.oopsmellanalyzer.model.CodeClass;
import java.util.List;

public final class ShotgunSurgeryRule implements CodeSmellRule {
    private static final int MIN_COLLABORATOR_TYPES = 5;
    @Override
    public List<SmellFinding> findIn(CodeClass codeClass, String sourceName) {
        if (codeClass.collaboratorTypes().size() < MIN_COLLABORATOR_TYPES) {
            return List.of();
        }
        return List.of(new SmellFinding("Shotgun Surgery", sourceName, codeClass.line(),
                "%s depends on %d collaborator types (limit: %d).".formatted(
                        codeClass.name(), codeClass.collaboratorTypes().size(), MIN_COLLABORATOR_TYPES),
                "Group related changes behind a focused collaborator or facade."));
    }
}
