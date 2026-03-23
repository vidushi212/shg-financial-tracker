package com.shg.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * UIUtility provides reusable console formatting methods for the SHG Financial
 * Tracking platform, including tables, headers, separators, menus, and value
 * formatting helpers.
 */
public class UIUtility {

    private static final int DEFAULT_WIDTH = 60;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ------------------------------------------------------------------ //
    //  Separators and headers
    // ------------------------------------------------------------------ //

    /** Prints a full-width line of '=' characters. */
    public static void printDoubleLine() {
        System.out.println("=".repeat(DEFAULT_WIDTH));
    }

    /** Prints a full-width line of '-' characters. */
    public static void printSingleLine() {
        System.out.println("-".repeat(DEFAULT_WIDTH));
    }

    /**
     * Prints a centred title surrounded by double-line separators.
     *
     * @param title the text to display as the header
     */
    public static void printHeader(String title) {
        printDoubleLine();
        System.out.println(centre(title, DEFAULT_WIDTH));
        printDoubleLine();
    }

    /**
     * Prints a section sub-header with a single-line separator below it.
     *
     * @param title the section label
     */
    public static void printSubHeader(String title) {
        System.out.println("\n  " + title);
        printSingleLine();
    }

    // ------------------------------------------------------------------ //
    //  Menus
    // ------------------------------------------------------------------ //

    /**
     * Displays a numbered menu from the supplied option list.
     *
     * @param options the list of option labels to show
     */
    public static void printMenu(List<String> options) {
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, options.get(i));
        }
        printSingleLine();
        System.out.print("  Enter your choice: ");
    }

    /**
     * Displays a prompt and waits for the user to enter text.
     *
     * @param prompt the label to display before the input cursor
     */
    public static void printInputPrompt(String prompt) {
        System.out.print("  " + prompt + ": ");
    }

    // ------------------------------------------------------------------ //
    //  Messages
    // ------------------------------------------------------------------ //

    /**
     * Prints a success message prefixed with a checkmark symbol.
     *
     * @param message the success text
     */
    public static void printSuccess(String message) {
        System.out.println("\n  ✔  " + message);
    }

    /**
     * Prints an error message prefixed with a cross symbol.
     *
     * @param message the error text
     */
    public static void printError(String message) {
        System.out.println("\n  ✖  ERROR: " + message);
    }

    /**
     * Prints an informational message prefixed with an info symbol.
     *
     * @param message the informational text
     */
    public static void printInfo(String message) {
        System.out.println("  ℹ  " + message);
    }

    // ------------------------------------------------------------------ //
    //  Table helpers
    // ------------------------------------------------------------------ //

    /**
     * Prints a simple two-column key/value row with fixed column widths.
     *
     * @param key   the left-hand label
     * @param value the right-hand value
     */
    public static void printTableRow(String key, String value) {
        System.out.printf("  %-28s : %s%n", key, value);
    }

    /**
     * Prints a table row with three columns.
     *
     * @param col1 first column text
     * @param col2 second column text
     * @param col3 third column text
     */
    public static void printTableRow3(String col1, String col2, String col3) {
        System.out.printf("  %-20s %-20s %-15s%n", col1, col2, col3);
    }

    /**
     * Prints a table header row with three columns.
     *
     * @param h1 first header label
     * @param h2 second header label
     * @param h3 third header label
     */
    public static void printTableHeader3(String h1, String h2, String h3) {
        printTableRow3(h1, h2, h3);
        printSingleLine();
    }

    /**
     * Prints a table row with four columns.
     *
     * @param col1 first column text
     * @param col2 second column text
     * @param col3 third column text
     * @param col4 fourth column text
     */
    public static void printTableRow4(String col1, String col2, String col3, String col4) {
        System.out.printf("  %-18s %-14s %-14s %-10s%n", col1, col2, col3, col4);
    }

    /**
     * Prints a table header row with four columns.
     *
     * @param h1 first header label
     * @param h2 second header label
     * @param h3 third header label
     * @param h4 fourth header label
     */
    public static void printTableHeader4(String h1, String h2, String h3, String h4) {
        printTableRow4(h1, h2, h3, h4);
        printSingleLine();
    }

    // ------------------------------------------------------------------ //
    //  Value formatters
    // ------------------------------------------------------------------ //

    /**
     * Formats a monetary value as an Indian Rupee string.
     *
     * @param amount the amount to format
     * @return a string like "₹ 1,234.50"
     */
    public static String formatCurrency(double amount) {
        return String.format("₹ %,.2f", amount);
    }

    /**
     * Formats a {@link LocalDate} using dd-MM-yyyy pattern.
     *
     * @param date the date to format
     * @return formatted date string
     */
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    /**
     * Returns today's date formatted as dd-MM-yyyy.
     *
     * @return formatted string for today
     */
    public static String today() {
        return formatDate(LocalDate.now());
    }

    // ------------------------------------------------------------------ //
    //  Misc helpers
    // ------------------------------------------------------------------ //

    /**
     * Centres a string within the given width by padding with spaces.
     *
     * @param text  the string to centre
     * @param width the total width to fill
     * @return a padded string centred within {@code width} characters
     */
    public static String centre(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        int leftPad = (width - text.length()) / 2;
        return " ".repeat(leftPad) + text;
    }

    /** Prints a blank line for visual spacing. */
    public static void printBlankLine() {
        System.out.println();
    }

    /**
     * Pauses execution and displays a "Press ENTER to continue" prompt.
     * Reads and discards one line of input.
     *
     * @param scanner the active {@link java.util.Scanner} bound to System.in
     */
    public static void pauseForInput(java.util.Scanner scanner) {
        System.out.print("\n  Press ENTER to continue...");
        scanner.nextLine();
    }
}
