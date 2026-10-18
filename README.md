# Stats Calculator

A small Java Swing app that reads a list of numbers from a `.txt` file and shows the mean, median, mode, and standard deviation in a text area. The GUI was built in Eclipse with WindowBuilder.

## How it works

1. Click **Get File** and pick a `.txt` file.
2. The file path appears in the label and the results show up in the text area.

Example output:

```
File: C:\data\test1_basic.txt
Count: 8

Mean: 5.0000
Median: 4.5000
Mode: 4.0
Standard Deviation: 2.0000
```

## Input format

Numbers can be separated by newlines, commas, spaces, or tabs, and you can mix them. Blank lines are ignored. Decimals, negatives, and scientific notation (`1e3`) all work.

```
2
4, 4, 4
5 5
7	9
```

Anything that isn't a number gets skipped and listed at the top of the results as `Skipped invalid value: "abc"`. The rest of the file is still used.

## Notes on the math

- **Standard deviation is the population version** (divides by `n`). For the sample version, change `sqDiffSum / n` to `sqDiffSum / (n - 1)` in `StatsCalculator.java` and update the expected values in the tests.
- **Ties in the mode are all reported**, in ascending order. If every value appears once, it says there is no mode.
- **Median** is the average of the two middle values when the count is even.

## Project layout

| File | Purpose |
|---|---|
| `StatsCalculator.java` | All the logic. Takes a file path, returns the results as a `String`. |
| `StatsCalculatorTest.java` | JUnit 5 tests for the calculator. |
| `[YourGUIClass].java` | The WindowBuilder frame. Handles the file chooser and calls `StatsCalculator.calculateResults(filePath)`. |

The calculation code lives in its own class so it can be tested without opening the window.

## Running it

You need Java 11 or newer.

In Eclipse: open the GUI class and use **Run As > Java Application**.

## Running the tests

Add JUnit 5 to the project (**Build Path > Add Libraries > JUnit > JUnit 5**), then right-click `StatsCalculatorTest.java` and choose **Run As > JUnit Test**.

The tests cover the basic cases (one value, two values, odd and even counts), different separators, messy input, bad tokens, empty and missing files, tied modes, large values, and a 10,000-number file. Temp files are created and cleaned up automatically.

## Known limitations

- `Double.parseDouble` accepts `NaN` and `Infinity`, so a file containing them will produce NaN results instead of a skipped-value warning. Adding a `Double.isFinite()` check would fix it.
- The whole file is loaded into memory, which is fine for normal data sets but not for huge ones.
- Only `.txt` files show up in the file chooser by default.
