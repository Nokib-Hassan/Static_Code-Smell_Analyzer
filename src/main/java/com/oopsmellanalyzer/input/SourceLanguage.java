package com.oopsmellanalyzer.input;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum SourceLanguage {
    JAVA(".java"),
    CSHARP(".cs");

    private final String extension;

    SourceLanguage(String extension) {
        this.extension = extension;
    }

    public static Optional<SourceLanguage> fromFileName(String fileName) {
        String normalizedName = fileName.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(language -> normalizedName.endsWith(language.extension))
                .findFirst();
    }
}
