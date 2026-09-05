package com.oopsmellanalyzer.parsing;

import com.oopsmellanalyzer.input.SourceLanguage;
import com.oopsmellanalyzer.input.SourceUnit;
import com.oopsmellanalyzer.model.CodeClass;
import com.oopsmellanalyzer.model.CodeMethod;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class CSharpSourceAnalyzer implements SourceAnalyzer {
    private static final Pattern CLASS_PATTERN = Pattern.compile(
            "\\b(?:class|record|struct)\\s+(?<name>[A-Za-z_]\\w*)(?:\\s*:\\s*(?<bases>[^\\{]+))?\\s*\\{");
    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "(?:(?:public|private|protected|internal|static|virtual|override|async|sealed|new|partial|extern|unsafe)\\s+)*"
                    + "(?<return>[A-Za-z_]\\w*(?:[<>,?\\[\\].\\s]+)?)\\s+(?<name>[A-Za-z_]\\w*)"
                    + "\\s*\\((?<parameters>[^)]*)\\)\\s*\\{");
    private static final Pattern FIELD_PATTERN = Pattern.compile(
            "(?m)^\\s*(?:(?:public|private|protected|internal|static|readonly|const|volatile|new)\\s+)*"
                    + "(?<type>[A-Za-z_]\\w*(?:[<>,?\\[\\].]+)?)\\s+(?<names>[A-Za-z_]\\w*(?:\\s*=\\s*[^;,]+)?"
                    + "(?:\\s*,\\s*[A-Za-z_]\\w*(?:\\s*=\\s*[^;,]+)?)*?)\\s*;");
    private static final Pattern METHOD_CALL_PATTERN = Pattern.compile("\\b(?<target>[A-Za-z_]\\w*)\\s*\\.\\s*\\w+\\s*\\(");
    private static final Pattern FIELD_ACCESS_PATTERN = Pattern.compile(
            "\\b(?<target>[A-Za-z_]\\w*)\\s*\\.\\s*\\w+\\b(?!\\s*\\()");
    private static final Pattern TYPE_PATTERN = Pattern.compile("\\b[A-Z][A-Za-z0-9_]*\\b");

    @Override
    public SourceLanguage language() {
        return SourceLanguage.CSHARP;
    }

    @Override
    public List<CodeClass> parse(SourceUnit unit) {
        String sanitizedSource = sanitize(unit.content());
        Matcher classMatcher = CLASS_PATTERN.matcher(sanitizedSource);
        List<CodeClass> classes = new ArrayList<>();
        while (classMatcher.find()) {
            int bodyStart = classMatcher.end() - 1;
            int bodyEnd = closingBrace(sanitizedSource, bodyStart);
            if (bodyEnd < 0) {
                throw new SourceParseException("C# source has an unclosed class body.");
            }
            classes.add(toCodeClass(unit.content(), sanitizedSource, classMatcher, bodyStart, bodyEnd));
        }
        return List.copyOf(classes);
    }

    private CodeClass toCodeClass(String source, String sanitizedSource, Matcher classMatcher,
                                  int bodyStart, int bodyEnd) {
        String name = classMatcher.group("name");
        String classBody = sanitizedSource.substring(bodyStart + 1, bodyEnd);
        List<MethodMatch> methodMatches = findMethods(sanitizedSource, bodyStart + 1, bodyEnd);
        List<CodeMethod> methods = methodMatches.stream()
                .map(match -> toCodeMethod(source, sanitizedSource, match))
                .toList();
        Set<String> collaboratorTypes = collaboratorTypes(classMatcher.group("bases"), classBody, methodMatches);
        int fieldCount = fieldCount(classBody, methodMatches, bodyStart + 1);
        return new CodeClass(name, lineOf(source, classMatcher.start()), fieldCount, methods,
                classMatcher.group("bases") != null, collaboratorTypes);
    }

    private List<MethodMatch> findMethods(String source, int bodyStart, int bodyEnd) {
        Matcher methodMatcher = METHOD_PATTERN.matcher(source);
        methodMatcher.region(bodyStart, bodyEnd);
        List<MethodMatch> methods = new ArrayList<>();
        while (methodMatcher.find()) {
            int openingBrace = methodMatcher.end() - 1;
            int closingBrace = closingBrace(source, openingBrace);
            if (closingBrace < 0 || closingBrace > bodyEnd) {
                throw new SourceParseException("C# source has an unclosed method body.");
            }
            methods.add(new MethodMatch(methodMatcher.group("name"), methodMatcher.group("parameters"),
                    methodMatcher.group(), methodMatcher.start(), openingBrace, closingBrace));
        }
        return List.copyOf(methods);
    }

    private CodeMethod toCodeMethod(String source, String sanitizedSource, MethodMatch match) {
        String body = sanitizedSource.substring(match.openingBrace() + 1, match.closingBrace());
        return new CodeMethod(match.name(), lineOf(source, match.start()),
                lineOf(source, match.closingBrace()) - lineOf(source, match.start()) + 1,
                parameterCount(match.parameters()), namedCounts(METHOD_CALL_PATTERN, body),
                namedCounts(FIELD_ACCESS_PATTERN, body), match.header().contains("override"), body.isBlank(),
                body.contains("throw new NotSupportedException") || body.contains("throw new NotImplementedException"));
    }

    private int fieldCount(String classBody, List<MethodMatch> methods, int bodyOffset) {
        char[] fieldOnlyBody = classBody.toCharArray();
        for (MethodMatch method : methods) {
            int start = method.start() - bodyOffset;
            int end = method.closingBrace() - bodyOffset;
            for (int index = Math.max(0, start); index <= Math.min(fieldOnlyBody.length - 1, end); index++) {
                if (fieldOnlyBody[index] != '\n') {
                    fieldOnlyBody[index] = ' ';
                }
            }
        }
        Matcher fieldMatcher = FIELD_PATTERN.matcher(new String(fieldOnlyBody));
        int fields = 0;
        while (fieldMatcher.find()) {
            fields += fieldMatcher.group("names").split(",").length;
        }
        return fields;
    }

    private Set<String> collaboratorTypes(String bases, String classBody, List<MethodMatch> methods) {
        Set<String> types = new HashSet<>();
        addTypes(types, bases == null ? "" : bases);
        Matcher fieldMatcher = FIELD_PATTERN.matcher(classBody);
        while (fieldMatcher.find()) {
            addTypes(types, fieldMatcher.group("type"));
        }
        for (MethodMatch method : methods) {
            addTypes(types, method.parameters());
        }
        types.removeAll(CommonTypes.NAMES);
        return Set.copyOf(types);
    }

    private void addTypes(Set<String> types, String text) {
        Matcher typeMatcher = TYPE_PATTERN.matcher(text);
        while (typeMatcher.find()) {
            types.add(typeMatcher.group());
        }
    }

    private Map<String, Long> namedCounts(Pattern pattern, String body) {
        Matcher matcher = pattern.matcher(body);
        List<String> names = new ArrayList<>();
        while (matcher.find()) {
            names.add(matcher.group("target"));
        }
        return names.stream().collect(Collectors.groupingBy(name -> name, Collectors.counting()));
    }

    private int parameterCount(String parameters) {
        return parameters.isBlank() ? 0 : parameters.split(",").length;
    }

    private int closingBrace(String source, int openingBrace) {
        int depth = 0;
        for (int index = openingBrace; index < source.length(); index++) {
            if (source.charAt(index) == '{') {
                depth++;
            } else if (source.charAt(index) == '}' && --depth == 0) {
                return index;
            }
        }
        return -1;
    }

    private int lineOf(String source, int index) {
        return (int) source.substring(0, index).chars().filter(character -> character == '\n').count() + 1;
    }

    private String sanitize(String source) {
        StringBuilder sanitized = new StringBuilder(source.length());
        boolean lineComment = false;
        boolean blockComment = false;
        boolean quoted = false;
        for (int index = 0; index < source.length(); index++) {
            char current = source.charAt(index);
            char next = index + 1 < source.length() ? source.charAt(index + 1) : '\0';
            if (lineComment && current == '\n') {
                lineComment = false;
            } else if (blockComment && current == '*' && next == '/') {
                sanitized.append("  ");
                index++;
                blockComment = false;
                continue;
            } else if (!quoted && !blockComment && current == '/' && next == '/') {
                sanitized.append("  ");
                index++;
                lineComment = true;
                continue;
            } else if (!quoted && !lineComment && current == '/' && next == '*') {
                sanitized.append("  ");
                index++;
                blockComment = true;
                continue;
            } else if (!lineComment && !blockComment && current == '"' && (index == 0 || source.charAt(index - 1) != '\\')) {
                quoted = !quoted;
            }
            sanitized.append(lineComment || blockComment || quoted ? (current == '\n' ? '\n' : ' ') : current);
        }
        return sanitized.toString();
    }

    private record MethodMatch(String name, String parameters, String header, int start, int openingBrace,
                               int closingBrace) {
    }
}
