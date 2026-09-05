package com.oopsmellanalyzer.analysis;

import com.oopsmellanalyzer.model.CodeClass;
import java.util.List;

public interface CodeSmellRule {
    List<SmellFinding> findIn(CodeClass codeClass, String sourceName);
}
