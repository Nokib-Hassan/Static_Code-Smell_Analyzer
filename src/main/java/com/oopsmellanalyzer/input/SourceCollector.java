package com.oopsmellanalyzer.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class SourceCollector {
    private static final int MAX_SOURCE_BYTES = 5 * 1024 * 1024;

    public List<SourceUnit> collect(Path input) throws IOException {
        if (!Files.exists(input)) {
            throw new IllegalArgumentException("Path does not exist: " + input);
        }
        if (Files.isDirectory(input)) {
            return collectDirectory(input);
        }
        if (isSupportedSource(input.getFileName().toString())) {
            return List.of(readFile(input));
        }
        if (isZipArchive(input.getFileName().toString())) {
            return collectZip(input);
        }
        throw new IllegalArgumentException("Input must be a .java file, .cs file, folder, or .zip archive.");
    }

    private List<SourceUnit> collectDirectory(Path directory) throws IOException {
        try (var paths = Files.walk(directory)) {
            List<SourceUnit> units = new ArrayList<>();
            for (Path path : paths.filter(Files::isRegularFile).filter(this::isJavaFile).toList()) {
                units.add(readFile(path));
            }
            return List.copyOf(units);
        }
    }

    private List<SourceUnit> collectZip(Path archive) throws IOException {
        try (ZipFile zipFile = new ZipFile(archive.toFile())) {
            List<SourceUnit> units = new ArrayList<>();
            var entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && isSupportedSource(entry.getName())) {
                    try (InputStream content = zipFile.getInputStream(entry)) {
                        units.add(new SourceUnit(archive + "!" + entry.getName(), readContent(content)));
                    }
                }
            }
            return List.copyOf(units);
        }
    }

    private SourceUnit readFile(Path file) throws IOException {
        try (InputStream content = Files.newInputStream(file)) {
            return new SourceUnit(file.toString(), readContent(content));
        }
    }

    private String readContent(InputStream input) throws IOException {
        byte[] bytes = input.readNBytes(MAX_SOURCE_BYTES + 1);
        if (bytes.length > MAX_SOURCE_BYTES) {
            throw new IllegalArgumentException("A source file is larger than 5 MB.");
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private boolean isJavaFile(Path path) {
        return isSupportedSource(path.getFileName().toString());
    }

    private boolean isSupportedSource(String fileName) {
        return SourceLanguage.fromFileName(fileName).isPresent();
    }

    private boolean isZipArchive(String fileName) {
        return fileName.toLowerCase(Locale.ROOT).endsWith(".zip");
    }
}
