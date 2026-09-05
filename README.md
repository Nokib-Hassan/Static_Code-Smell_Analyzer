# Static_Code-Smell_Analyzer

This is a small, cross-platform command-line program that reads **Java and C#** code and reports possible object-oriented design problems, called **code smells**. A smell is not automatically a bug. It is a signal that a person should inspect the code and decide whether it can be made easier to understand or maintain.

The tool is implemented in Java. JavaParser reads Java into an **abstract syntax tree (AST)**. A pure-Java C# structural parser reads C# class, field, method, inheritance, call, and direct-field-access information. Both adapters produce the same neutral model, so every smell rule works for both input languages.

## 1. What you need

Install these two tools once:

1. **JDK 21 or newer**. It runs Java programs.
2. **Apache Maven 3.9 or newer**. It downloads libraries and builds the project.

Check them in a terminal:

```bash
java -version
mvn -version
```

These commands work on macOS, Windows, and Linux after Java and Maven are installed. The project deliberately targets Java 21, an LTS release, even if you have a newer JDK installed.

## 2. First run

Open a terminal in this project folder, then copy and paste:

```bash
mvn test
mvn exec:java -Dexec.args="examples/SmellyOrder.java"
mvn exec:java -Dexec.args="examples/AllSmells.java"
mvn exec:java -Dexec.args="examples/AllSmells.cs"
```

The first command downloads JavaParser and runs tests. The second command reports the first two smells. The final two commands report every currently supported smell in Java and C# respectively.

To analyze your own code, replace the example path:

```bash
mvn exec:java -Dexec.args="/full/path/to/MyClass.java"
mvn exec:java -Dexec.args="/full/path/to/MyClass.cs"
mvn exec:java -Dexec.args="/full/path/to/mixed-java-csharp-project"
mvn exec:java -Dexec.args="/full/path/to/submission.zip"
```

The ZIP option reads `.java` and `.cs` files directly from the archive. It does not extract files onto your computer. Each source file is capped at 5 MB to avoid accidentally processing an unexpectedly huge archive entry.

## 3. How the program works

```text
File / folder / ZIP
        |
        v
SourceCollector       reads every .java and .cs file
        |
        v
JavaSourceAnalyzer / CSharpSourceAnalyzer
        |
        v
CodeClass / CodeMethod neutral model
        |
        v
CodeSmellRule objects run one rule each
        |
        v
Terminal report
```

The analyzer uses transparent learning heuristics. They are useful prompts for review, not proof that code must be changed:

| Rule | Detection heuristic | What to consider |
| --- | --- | --- |
| Long Method | More than 10 source lines | Can part of the method become a named helper method? |
| Large Class | More than 3 fields | Does this class have multiple responsibilities? |
| God Class | At least 5 fields and 10 methods | Split separate responsibilities into focused classes. |
| Feature Envy | One method calls one named collaborator at least 4 times | Would the behavior make more sense on that collaborator? |
| Data Class | At least 3 fields and only `get*`, `is*`, or `set*` methods | Is useful behavior living outside the class instead? |
| Inappropriate Intimacy | One method directly reads at least 4 fields from one named collaborator | Can the collaborator hide its details behind methods? |
| Refused Bequest | A subclass override is empty or throws `UnsupportedOperationException` | Would composition or a narrower parent type describe this design better? |
| Shotgun Surgery | One class mentions at least 5 non-basic collaborator types | Can a facade or smaller collaborator group reduce change coupling? |

These are learning thresholds, not universal industry standards. The tool does not yet resolve types across a complete project, so Feature Envy, Inappropriate Intimacy, and Shotgun Surgery use AST patterns rather than complete semantic knowledge. Treat every finding as a human-review prompt.

## 4. Project map

```text
src/main/java/com/oopsmellanalyzer/
  App.java                     command-line entry point
  input/SourceCollector.java   file, folder, and ZIP input
  input/SourceUnit.java        one source file in memory
  analysis/AnalyzerService.java chooses a parser and coordinates rules
  analysis/CodeSmellRule.java   shared shape for every smell rule
  analysis/LongMethodRule.java  first rule
  analysis/LargeClassRule.java  second rule
  analysis/*Rule.java           remaining independent smell rules
  analysis/SmellFinding.java    one result shown to the user
  parsing/JavaSourceAnalyzer.java JavaParser adapter
  parsing/CSharpSourceAnalyzer.java C# structural parser
  parsing/SourceAnalyzer.java   extension point for another language
  model/CodeClass.java          language-neutral class information
  model/CodeMethod.java         language-neutral method information
src/test/java/                 automated checks
examples/SmellyOrder.java      safe practice input
examples/AllSmells.java        one example for every supported smell
examples/AllSmells.cs          C# example for every supported smell
```

## 5. Reading the important code

`App.java` is where the program starts. It accepts exactly one input path, asks `SourceCollector` for source files, sends them to `AnalyzerService`, and prints the findings.

`SourceCollector.java` has one job: turn a single input into a list of `SourceUnit` records. It recognizes `.java` and `.cs` files directly, inside a folder, or inside a `.zip` archive. Each `SourceUnit` records its input language.

`AnalyzerService.java` selects a parser from the `SourceUnit` language. `JavaSourceAnalyzer` uses JavaParser; `CSharpSourceAnalyzer` walks C# declarations with a comment- and string-aware structural parser. Each produces `CodeClass` and `CodeMethod` records. The service then gives that neutral information to every `CodeSmellRule`.

Every `*Rule.java` file gets one class AST at a time and either returns no result or creates a `SmellFinding`. For example, `LongMethodRule.java` calculates a method's AST line range; `FeatureEnvyRule.java` counts calls made through the same named variable; and `RefusedBequestRule.java` looks for an unsupported `@Override` in a subclass.

## 6. Add your next smell rule

When you are ready, create a new class such as `TooManyMethodsRule` in the `analysis` package. It must implement `CodeSmellRule`. Then add one instance to the list in `AnalyzerService`:

```java
this(List.of(new LongMethodRule(), new LargeClassRule(), new TooManyMethodsRule()));
```

The analyzer now includes the six additional OOP smells. Good next beginner rules are:

1. **Too Many Methods**: flag a class with more than a chosen number of methods.
2. **Long Parameter List**: flag methods with more than a chosen number of parameters.
3. **Switch Statements**: flag large switches that may be replaceable with polymorphism.

For every new rule, first add a test in `src/test/java`, make it fail, then write the smallest implementation that makes it pass. This protects existing behavior as your analyzer grows.

## 7. Add another input language

The input layer is independent of individual parsers. To add a language later:

1. Add its extension to `SourceLanguage`.
2. Implement `SourceAnalyzer` to create `CodeClass` and `CodeMethod` values.
3. Register that analyzer in `AnalyzerService`.
4. Add source and ZIP collector tests plus smell tests using the new extension.

Keep the shared model small and only add a property when a rule genuinely needs it. This prevents language-specific syntax from leaking into every smell rule.

## 8. C# parser scope

The C# parser supports normal class, record, and struct declarations; fields; brace-bodied methods; inheritance; method calls; direct field access; and `override` methods that throw `NotSupportedException` or `NotImplementedException`. It safely ignores ordinary quoted strings and comments when matching braces.

It is intentionally a lightweight structural parser, not a replacement for the Roslyn compiler. Advanced C# syntax such as source generators, expression-bodied members, preprocessor-driven declarations, and complete type resolution are future work. Findings remain review prompts, especially across complex projects.

## 9. Useful Maven commands

```bash
mvn test       # run tests
mvn compile    # compile the main program
mvn package    # create a build in target/
mvn clean      # remove generated build files
```

`target/` is generated by Maven and is ignored by Git. Do not put your source code there.
