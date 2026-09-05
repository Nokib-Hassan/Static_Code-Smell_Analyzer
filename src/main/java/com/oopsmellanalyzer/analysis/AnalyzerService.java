package com.oopsmellanalyzer.analysis;

import com.oopsmellanalyzer.input.SourceLanguage;
import com.oopsmellanalyzer.input.SourceUnit;
import com.oopsmellanalyzer.model.CodeClass;
import com.oopsmellanalyzer.parsing.CSharpSourceAnalyzer;
import com.oopsmellanalyzer.parsing.JavaSourceAnalyzer;
import com.oopsmellanalyzer.parsing.SourceAnalyzer;
import com.oopsmellanalyzer.parsing.SourceParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class AnalyzerService {
    private final List<CodeSmellRule> rules;
    private final Map<SourceLanguage, SourceAnalyzer> sourceAnalyzers;

    public AnalyzerService() {
        this(List.of(
                new LongMethodRule(),
                new LargeClassRule(),
                new GodClassRule(),
                new FeatureEnvyRule(),
                new DataClassRule(),
                new InappropriateIntimacyRule(),
                new RefusedBequestRule(),
                new ShotgunSurgeryRule()), List.of(new JavaSourceAnalyzer(), new CSharpSourceAnalyzer()));
    }

    AnalyzerService(List<CodeSmellRule> rules) {
        this(rules, List.of(new JavaSourceAnalyzer(), new CSharpSourceAnalyzer()));
    }

    AnalyzerService(List<CodeSmellRule> rules, List<SourceAnalyzer> sourceAnalyzers) {
        this.rules = List.copyOf(rules);
        this.sourceAnalyzers = sourceAnalyzers.stream()
                .collect(Collectors.toUnmodifiableMap(SourceAnalyzer::language, Function.identity()));
    }

    public List<SmellFinding> analyze(List<SourceUnit> units) {
        List<SmellFinding> findings = new ArrayList<>();
        for (SourceUnit unit : units) {
            findings.addAll(analyze(unit));
        }
        return List.copyOf(findings);
    }

    public List<SmellFinding> analyze(SourceUnit unit) {
        try {
            SourceAnalyzer sourceAnalyzer = sourceAnalyzers.get(unit.language());
            if (sourceAnalyzer == null) {
                throw new IllegalArgumentException("No analyzer is registered for " + unit.language());
            }
            List<SmellFinding> findings = new ArrayList<>();
            for (CodeClass codeClass : sourceAnalyzer.parse(unit)) {
                for (CodeSmellRule rule : rules) {
                    findings.addAll(rule.findIn(codeClass, unit.displayName()));
                }
            }
            return List.copyOf(findings);
        } catch (SourceParseException exception) {
            return List.of(new SmellFinding("Parse Error", unit.displayName(), 1,
                    exception.getMessage(), "Fix the source syntax, then run the analyzer again."));
        }
    }
}
