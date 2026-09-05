package com.oopsmellanalyzer.parsing;

import com.oopsmellanalyzer.input.SourceLanguage;
import com.oopsmellanalyzer.input.SourceUnit;
import com.oopsmellanalyzer.model.CodeClass;
import java.util.List;

public interface SourceAnalyzer {
    SourceLanguage language();

    List<CodeClass> parse(SourceUnit unit);
}
