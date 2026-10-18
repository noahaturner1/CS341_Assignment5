package tester;

import turner.StatsCalculator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StatsCalculatorTest {

    @TempDir
    Path tempDir;

    @BeforeAll
    static void useUsLocale() {
        // String.format uses the default locale; force "." decimals
        Locale.setDefault(Locale.US);
    }

    // ---------- helpers ----------

    private String run(String content) throws IOException {
        Path file = Files.createTempFile(tempDir, "data", ".txt");
        Files.writeString(file, content);
        return StatsCalculator.calculateResults(file.toString());
    }

    private String line(String result, String prefix) {
        return result.lines()
                .map(String::trim)
                .filter(l -> l.startsWith(prefix))
                .findFirst()
                .orElse("<missing line: " + prefix + ">");
    }

    private void assertStats(String r, String count, String mean,
                             String median, String mode, String std) {
        assertEquals("Count: " + count, line(r, "Count:"));
        assertEquals("Mean: " + mean, line(r, "Mean:"));
        assertEquals("Median: " + median, line(r, "Median:"));
        assertEquals("Mode: " + mode, line(r, "Mode:"));
        assertEquals("Standard Deviation: " + std, line(r, "Standard Deviation:"));
    }

    // ---------- core test sets ----------

    @Test
    void basicOnePerLine() throws IOException {
        String r = run("2\n4\n4\n4\n5\n5\n7\n9\n");
        assertStats(r, "8", "5.0000", "4.5000", "4.0", "2.0000");
    }

    @Test
    void commaSeparatedSingleLine() throws IOException {
        String r = run("10, 20, 30, 40, 50");
        assertStats(r, "5", "30.0000", "30.0000", "No mode (all values unique)", "14.1421");
    }

    @Test
    void decimalsWithTwoModes() throws IOException {
        String r = run("1.5\n2.5\n2.5\n3.5\n4.5\n4.5\n6.0\n");
        assertStats(r, "7", "3.5714", "3.5000", "2.5, 4.5", "1.4250");
    }

    @Test
    void negativesBlankLinesAndMixedSpacing() throws IOException {
        String r = run("-5\n\n0   5\n10\n");
        assertStats(r, "4", "2.5000", "2.5000", "No mode (all values unique)", "5.5902");
    }

    @Test
    void singleValue() throws IOException {
        String r = run("42");
        assertStats(r, "1", "42.0000", "42.0000", "No mode (all values unique)", "0.0000");
    }

    @Test
    void invalidTokenIsSkippedAndReported() throws IOException {
        String r = run("3\n5\nabc\n7\n");
        assertTrue(r.startsWith("Skipped invalid value: \"abc\""));
        assertStats(r, "3", "5.0000", "5.0000", "No mode (all values unique)", "1.6330");
    }

    @Test
    void emptyFile() throws IOException {
        assertEquals("No valid numbers found in the file.", run("").trim());
    }

    // ---------- input handling ----------

    @Test
    void nullPathPromptsForFile() {
        assertEquals("Please select a file first.", StatsCalculator.calculateResults(null));
    }

    @Test
    void emptyPathPromptsForFile() {
        assertEquals("Please select a file first.", StatsCalculator.calculateResults(""));
    }

    @Test
    void missingFileReportsError() {
        String r = StatsCalculator.calculateResults(tempDir.resolve("nope.txt").toString());
        assertTrue(r.startsWith("Could not read file"));
    }

    @Test
    void whitespaceOnlyFile() throws IOException {
        assertTrue(run("   \n\n \t \n").contains("No valid numbers found in the file."));
    }

    @Test
    void onlyInvalidTokens() throws IOException {
        String r = run("abc\nxyz\n");
        assertTrue(r.contains("Skipped invalid value: \"abc\""));
        assertTrue(r.contains("Skipped invalid value: \"xyz\""));
        assertTrue(r.contains("No valid numbers found in the file."));
    }

    @Test
    void multipleInvalidTokensAllReportedValidOnesStillCounted() throws IOException {
        String r = run("a\nb\n1\n2\n");
        assertTrue(r.contains("Skipped invalid value: \"a\""));
        assertTrue(r.contains("Skipped invalid value: \"b\""));
        assertStats(r, "2", "1.5000", "1.5000", "No mode (all values unique)", "0.5000");
    }

    @Test
    void windowsLineEndings() throws IOException {
        String r = run("1\r\n2\r\n3\r\n");
        assertStats(r, "3", "2.0000", "2.0000", "No mode (all values unique)", "0.8165");
    }

    @Test
    void tabSeparatedValues() throws IOException {
        String r = run("1\t2\t3");
        assertStats(r, "3", "2.0000", "2.0000", "No mode (all values unique)", "0.8165");
    }

    @Test
    void mixedCommasAndSpaces() throws IOException {
        String r = run("1, 2,3 ,4");
        assertStats(r, "4", "2.5000", "2.5000", "No mode (all values unique)", "1.1180");
    }

    @Test
    void noTrailingNewline() throws IOException {
        String r = run("1\n2\n3");
        assertEquals("Count: 3", line(r, "Count:"));
    }

    @Test
    void scientificNotation() throws IOException {
        String r = run("1e3\n2e3\n");
        assertStats(r, "2", "1500.0000", "1500.0000", "No mode (all values unique)", "500.0000");
    }

    @Test
    void outputIncludesFilePath() throws IOException {
        Path file = Files.createTempFile(tempDir, "data", ".txt");
        Files.writeString(file, "1\n2\n");
        String r = StatsCalculator.calculateResults(file.toString());
        assertTrue(r.contains("File: " + file));
    }

    // ---------- median ----------

    @Test
    void medianOddCountUnsortedInput() throws IOException {
        String r = run("9\n1\n5\n3\n7\n");
        assertStats(r, "5", "5.0000", "5.0000", "No mode (all values unique)", "2.8284");
    }

    @Test
    void medianEvenCountUnsortedInput() throws IOException {
        String r = run("10\n2\n8\n4\n");
        assertStats(r, "4", "6.0000", "6.0000", "No mode (all values unique)", "3.1623");
    }

    @Test
    void twoValues() throws IOException {
        String r = run("1\n3\n");
        assertStats(r, "2", "2.0000", "2.0000", "No mode (all values unique)", "1.0000");
    }

    // ---------- mode ----------

    @Test
    void allIdenticalValues() throws IOException {
        String r = run("5\n5\n5\n5\n");
        assertStats(r, "4", "5.0000", "5.0000", "5.0", "0.0000");
    }

    @Test
    void singleModeAmongRepeats() throws IOException {
        String r = run("1\n2\n2\n3\n3\n3\n");
        assertEquals("Mode: 3.0", line(r, "Mode:"));
    }

    @Test
    void threeWayTie() throws IOException {
        String r = run("1\n1\n2\n2\n3\n3\n");
        assertEquals("Mode: 1.0, 2.0, 3.0", line(r, "Mode:"));
    }

    @Test
    void modeListedOnceEvenWhenRepeatedManyTimes() throws IOException {
        String r = run("1\n1\n1\n1\n2\n2\n2\n2\n");
        assertStats(r, "8", "1.5000", "1.5000", "1.0, 2.0", "0.5000");
    }

    @Test
    void tiedModesWithNegativeAndPositiveSameMagnitude() throws IOException {
        // Regression: "2.5" is a substring of "-2.5" (bug in the old indexOf check)
        String r = run("-2.5\n-2.5\n2.5\n2.5\n");
        assertStats(r, "4", "0.0000", "0.0000", "-2.5, 2.5", "2.5000");
    }

    // ---------- magnitude / scale ----------

    @Test
    void largeValues() throws IOException {
        String r = run("1000000\n2000000\n3000000\n");
        assertStats(r, "3", "2000000.0000", "2000000.0000",
                "No mode (all values unique)", "816496.5809");
    }

    @Test
    void largeDataSet() throws IOException {
        String data = IntStream.rangeClosed(1, 10000)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining("\n"));
        String r = run(data);
        assertStats(r, "10000", "5000.5000", "5000.5000",
                "No mode (all values unique)", "2886.7513");
    }
}