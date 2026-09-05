package com.oopsmellanalyzer;

import com.oopsmellanalyzer.analysis.AnalyzerService;
import com.oopsmellanalyzer.analysis.SmellFinding;
import com.oopsmellanalyzer.input.SourceCollector;
import com.oopsmellanalyzer.input.SourceUnit;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

public final class App {
    private App() {
    }

    public static void main(String[] arguments) {
        int exitCode = new App().run(arguments, System.out, System.err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    int run(String[] arguments, PrintStream output, PrintStream errors) {
        if (arguments.length != 1) {
            errors.println("Usage: mvn exec:java -Dexec.args=\"<Java/C# file, folder, or ZIP>\"");
            return 2;
        }

        try {
            List<SourceUnit> sources = new SourceCollector().collect(Path.of(arguments[0]));
            List<SmellFinding> findings = new AnalyzerService().analyze(sources);
            printReport(sources.size(), findings, output);
            return 0;
        } catch (IOException | IllegalArgumentException exception) {
            errors.println("Could not analyze input: " + exception.getMessage());
            return 1;
        }
    }

    private void printReport(int sourceCount, List<SmellFinding> findings, PrintStream output) {
        output.printf("Analyzed %d supported source file(s).%n%n", sourceCount);

        if (findings.isEmpty()) {
            output.println("No smells found by the current rules.");
            return;
        }

        output.printf("Found %d issue(s):%n", findings.size());
        for (SmellFinding finding : findings) {
            output.printf("- [%s] %s:%d%n  %s%n  Suggestion: %s%n", finding.smellName(),
                    finding.sourceName(), finding.line(), finding.message(), finding.suggestion());
        }
    }
}
