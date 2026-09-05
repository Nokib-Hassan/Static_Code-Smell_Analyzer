package com.oopsmellanalyzer.parsing;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.oopsmellanalyzer.input.SourceLanguage;
import com.oopsmellanalyzer.input.SourceUnit;
import com.oopsmellanalyzer.model.CodeClass;
import com.oopsmellanalyzer.model.CodeMethod;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class JavaSourceAnalyzer implements SourceAnalyzer {
    @Override
    public SourceLanguage language() {
        return SourceLanguage.JAVA;
    }

    @Override
    public List<CodeClass> parse(SourceUnit unit) {
        try {
            return StaticJavaParser.parse(unit.content()).findAll(ClassOrInterfaceDeclaration.class).stream()
                    .filter(declaration -> !declaration.isInterface())
                    .map(this::toCodeClass)
                    .toList();
        } catch (ParseProblemException exception) {
            throw new SourceParseException("JavaParser could not read this file as valid Java.");
        }
    }

    private CodeClass toCodeClass(ClassOrInterfaceDeclaration declaration) {
        int fieldCount = declaration.getFields().stream().mapToInt(field -> field.getVariables().size()).sum();
        List<CodeMethod> methods = declaration.getMethods().stream().map(this::toCodeMethod).toList();
        Set<String> collaboratorTypes = declaration.findAll(ClassOrInterfaceType.class).stream()
                .map(ClassOrInterfaceType::getNameAsString)
                .filter(type -> !type.equals(declaration.getNameAsString()))
                .filter(type -> !CommonTypes.NAMES.contains(type))
                .collect(Collectors.toUnmodifiableSet());
        int line = declaration.getRange().map(range -> range.begin.line).orElse(1);
        return new CodeClass(declaration.getNameAsString(), line, fieldCount, methods,
                !declaration.getExtendedTypes().isEmpty(), collaboratorTypes);
    }

    private CodeMethod toCodeMethod(MethodDeclaration method) {
        int line = method.getRange().map(range -> range.begin.line).orElse(1);
        int lineCount = method.getRange().map(range -> range.end.line - range.begin.line + 1).orElse(0);
        boolean overridesMethod = method.getAnnotations().stream()
                .anyMatch(annotation -> annotation.getNameAsString().equals("Override"));
        boolean emptyBody = method.getBody().map(body -> body.getStatements().isEmpty()).orElse(false);
        boolean rejectsInheritedBehavior = method.findAll(ThrowStmt.class).stream()
                .map(throwStatement -> throwStatement.getExpression().toString())
                .anyMatch(expression -> expression.contains("UnsupportedOperationException"));
        return new CodeMethod(method.getNameAsString(), line, lineCount, method.getParameters().size(),
                namedMethodCalls(method), namedFieldAccesses(method), overridesMethod, emptyBody,
                rejectsInheritedBehavior);
    }

    private Map<String, Long> namedMethodCalls(MethodDeclaration method) {
        return method.findAll(MethodCallExpr.class).stream()
                .flatMap(call -> call.getScope().stream())
                .filter(NameExpr.class::isInstance)
                .map(NameExpr.class::cast)
                .map(NameExpr::getNameAsString)
                .collect(Collectors.groupingBy(name -> name, Collectors.counting()));
    }

    private Map<String, Long> namedFieldAccesses(MethodDeclaration method) {
        return method.findAll(FieldAccessExpr.class).stream()
                .map(FieldAccessExpr::getScope)
                .filter(NameExpr.class::isInstance)
                .map(NameExpr.class::cast)
                .map(NameExpr::getNameAsString)
                .collect(Collectors.groupingBy(name -> name, Collectors.counting()));
    }
}
