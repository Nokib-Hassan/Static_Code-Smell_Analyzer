package com.oopsmellanalyzer.input;

import java.util.Objects;

public record SourceUnit(String displayName, String content, SourceLanguage language) {
    public SourceUnit {
        Objects.requireNonNull(displayName, "displayName must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(language, "language must not be null");
    }

    public SourceUnit(String displayName, String content) {
        this(displayName, content, SourceLanguage.fromFileName(displayName)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported source file: " + displayName)));
    }
}
