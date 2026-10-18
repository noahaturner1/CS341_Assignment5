package turner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class StatsCalculator {

    public static String calculateResults(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "Please select a file first.";
        }

        ArrayList<Double> nums = new ArrayList<>();
        StringBuilder warnings = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                for (String token : line.split("[,\\s]+")) {
                    if (token.isEmpty()) continue;
                    try {
                        nums.add(Double.parseDouble(token));
                    } catch (NumberFormatException ex) {
                        warnings.append("Skipped invalid value: \"").append(token).append("\"\n");
                    }
                }
            }
        } catch (IOException ex) {
            return "Could not read file: " + ex.getMessage();
        }

        if (nums.isEmpty()) {
            return warnings + "No valid numbers found in the file.";
        }

        int n = nums.size();

        // Mean
        double sum = 0;
        for (double x : nums) sum += x;
        double mean = sum / n;

        // Median
        ArrayList<Double> sorted = new ArrayList<>(nums);
        Collections.sort(sorted);
        double median = (n % 2 == 0)
            ? (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0
            : sorted.get(n / 2);

        // Mode (supports ties)
        Map<Double, Integer> counts = new HashMap<>();
        int maxCount = 0;
        for (double x : nums) {
            int c = counts.merge(x, 1, Integer::sum);
            if (c > maxCount) maxCount = c;
        }
        String modeText;
        if (maxCount == 1) {
            modeText = "No mode (all values unique)";
        } else {
            TreeSet<Double> modes = new TreeSet<>();
            for (Map.Entry<Double, Integer> e : counts.entrySet()) {
                if (e.getValue() == maxCount) modes.add(e.getKey());
            }
            StringBuilder sb = new StringBuilder();
            for (double m : modes) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(m);
            }
            modeText = sb.toString();
        }

        // Standard deviation (population)
        double sqDiffSum = 0;
        for (double x : nums) sqDiffSum += Math.pow(x - mean, 2);
        double stdDev = Math.sqrt(sqDiffSum / n);

        return warnings +
            "File: " + filePath + "\n" +
            "Count: " + n + "\n\n" +
            String.format("Mean: %.4f%n", mean) +
            String.format("Median: %.4f%n", median) +
            "Mode: " + modeText + "\n" +
            String.format("Standard Deviation: %.4f%n", stdDev);
    }
}