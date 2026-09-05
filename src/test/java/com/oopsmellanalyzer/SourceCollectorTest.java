package com.oopsmellanalyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.oopsmellanalyzer.input.SourceCollector;
import com.oopsmellanalyzer.input.SourceUnit;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SourceCollectorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void readsJavaAndCSharpSourceFromZipWithoutExtractingIt() throws Exception {
        Path archive = temporaryDirectory.resolve("submission.zip");
        try (OutputStream fileOutput = java.nio.file.Files.newOutputStream(archive);
             ZipOutputStream zipOutput = new ZipOutputStream(fileOutput)) {
            zipOutput.putNextEntry(new ZipEntry("project/Example.java"));
            zipOutput.write("class Example {}".getBytes(StandardCharsets.UTF_8));
            zipOutput.closeEntry();
            zipOutput.putNextEntry(new ZipEntry("project/Example.cs"));
            zipOutput.write("class Example {}".getBytes(StandardCharsets.UTF_8));
            zipOutput.closeEntry();
        }

        List<SourceUnit> units = new SourceCollector().collect(archive);

        assertEquals(2, units.size());
        assertEquals("class Example {}", units.getFirst().content());
        assertEquals(archive + "!project/Example.java", units.getFirst().displayName());
        assertEquals(archive + "!project/Example.cs", units.get(1).displayName());
    }
}
