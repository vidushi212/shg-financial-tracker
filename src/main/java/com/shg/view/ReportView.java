package com.shg.view;

import java.util.List;
import java.util.Scanner;

/**
 * ReportView renders the console UI for the Reports module.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Display monthly financial summaries</li>
 *   <li>Show aggregated savings, loans, and expenses</li>
 *   <li>Present comparative tables in console format</li>
 *   <li>Display investment comparison reports</li>
 * </ul>
 */
public class ReportView {

    private final Scanner scanner;

    /**
     * Constructs a ReportView bound to the supplied scanner.
     *
     * @param scanner the active {@link Scanner} reading from System.in
     */
    public ReportView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------------------------------------------------------ //
    //  Module menu
    // ------------------------------------------------------------------ //

    /**
     * Displays the Reports module menu and returns the user's selection.
     *
     * @return {@code 1} Monthly Summary, {@code 2} Comparative Report,
     *         {@code 3} Investment Comparison, {@code 4} Back to Dashboard
     */
    public int showReportMenu() {
        UIUtility.printHeader("REPORTS MODULE");
        System.out.println("  1. Monthly Financial Summary");
        System.out.println("  2. Comparative Report (Multi-Month)");
        System.out.println("  3. Investment Comparison Report");
        System.out.println("  4. Back to Dashboard");
        UIUtility.printSingleLine();
        System.out.print("  Enter your choice: ");
        return readIntInput(1, 4);
    }

    // ------------------------------------------------------------------ //
    //  Month / year selection
    // ------------------------------------------------------------------ //

    /**
     * Prompts the user to enter a month and year for report generation.
     *
     * @return a two-element array: {@code [month (1-12), year (e.g. 2024)]}
     *         as strings
     */
    public String[] showMonthYearInput() {
        UIUtility.printSubHeader("Select Month & Year");

        int month = 0;
        while (true) {
            UIUtility.printInputPrompt("Month (1-12)");
            try {
                month = Integer.parseInt(scanner.nextLine().trim());
                if (month >= 1 && month <= 12) break;
                UIUtility.printError("Month must be between 1 and 12.");
            } catch (NumberFormatException e) {
                UIUtility.printError("Please enter a valid month number.");
            }
        }

        int year = 0;
        while (true) {
            UIUtility.printInputPrompt("Year (e.g. 2024)");
            try {
                year = Integer.parseInt(scanner.nextLine().trim());
                if (year >= 2000 && year <= 2100) break;
                UIUtility.printError("Please enter a valid year between 2000 and 2100.");
            } catch (NumberFormatException e) {
                UIUtility.printError("Please enter a valid year.");
            }
        }

        return new String[]{String.valueOf(month), String.valueOf(year)};
    }

    // ------------------------------------------------------------------ //
    //  Monthly summary
    // ------------------------------------------------------------------ //

    /**
     * Renders a monthly financial summary panel.
     *
     * @param month        the month label (e.g. "March 2024")
     * @param totalSavings total savings collected in that month
     * @param totalLoans   total loans disbursed in that month
     * @param totalExpenses total expenses recorded in that month
     * @param netBalance   net balance at end of month
     */
    public void showMonthlySummary(String month, double totalSavings,
                                   double totalLoans, double totalExpenses,
                                   double netBalance) {
        UIUtility.printSubHeader("Monthly Summary: " + month);
        UIUtility.printTableRow("Total Savings",   UIUtility.formatCurrency(totalSavings));
        UIUtility.printTableRow("Total Loans",     UIUtility.formatCurrency(totalLoans));
        UIUtility.printTableRow("Total Expenses",  UIUtility.formatCurrency(totalExpenses));
        UIUtility.printSingleLine();
        UIUtility.printTableRow("Net Balance",     UIUtility.formatCurrency(netBalance));
        UIUtility.printSingleLine();
    }

    // ------------------------------------------------------------------ //
    //  Comparative report (multi-month)
    // ------------------------------------------------------------------ //

    /**
     * Displays a multi-month comparative table.
     *
     * <p>Each entry in {@code rows} must be a four-element array:
     * {@code [month, savings, loans, expenses]}.
     *
     * @param rows the monthly aggregated data rows
     */
    public void showComparativeReport(List<String[]> rows) {
        UIUtility.printSubHeader("Comparative Financial Report");
        if (rows == null || rows.isEmpty()) {
            UIUtility.printInfo("No data available for comparison.");
            return;
        }
        UIUtility.printTableHeader4("Month", "Savings (₹)", "Loans (₹)", "Expenses (₹)");
        for (String[] row : rows) {
            UIUtility.printTableRow4(
                    row.length > 0 ? row[0] : "",
                    row.length > 1 ? row[1] : "",
                    row.length > 2 ? row[2] : "",
                    row.length > 3 ? row[3] : "");
        }
        UIUtility.printSingleLine();
    }

    /**
     * Renders a simple ASCII bar chart for monthly savings.
     *
     * <p>Each entry in {@code data} must be a two-element array:
     * {@code [label, value]}.
     *
     * @param title    the chart title
     * @param data     label/value pairs for the chart bars
     * @param maxValue the maximum value (used to scale bar widths)
     */
    public void showBarChart(String title, List<String[]> data, double maxValue) {
        UIUtility.printSubHeader(title);
        int barWidth = 30;
        for (String[] entry : data) {
            String label = entry.length > 0 ? entry[0] : "";
            double value = 0;
            try {
                value = Double.parseDouble(entry.length > 1 ? entry[1] : "0");
            } catch (NumberFormatException ignored) { }

            int filled = maxValue > 0 ? (int) ((value / maxValue) * barWidth) : 0;
            String bar = "█".repeat(filled) + "░".repeat(barWidth - filled);
            System.out.printf("  %-12s |%s| %s%n", label, bar, UIUtility.formatCurrency(value));
        }
        UIUtility.printSingleLine();
    }

    // ------------------------------------------------------------------ //
    //  Investment comparison
    // ------------------------------------------------------------------ //

    /**
     * Displays an investment comparison table.
     *
     * <p>Each entry in {@code plans} must be a four-element array:
     * {@code [planName, expectedReturn, risk, term]}.
     *
     * @param plans the list of investment plan comparison rows
     */
    public void showInvestmentComparison(List<String[]> plans) {
        UIUtility.printSubHeader("Investment Comparison Report");
        if (plans == null || plans.isEmpty()) {
            UIUtility.printInfo("No investment plans available for comparison.");
            return;
        }
        UIUtility.printTableHeader4("Plan Name", "Return (%)", "Risk Level", "Term");
        for (String[] plan : plans) {
            UIUtility.printTableRow4(
                    plan.length > 0 ? plan[0] : "",
                    plan.length > 1 ? plan[1] : "",
                    plan.length > 2 ? plan[2] : "",
                    plan.length > 3 ? plan[3] : "");
        }
        UIUtility.printSingleLine();
    }

    // ------------------------------------------------------------------ //
    //  Input helpers
    // ------------------------------------------------------------------ //

    private int readIntInput(int min, int max) {
        while (true) {
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  Please enter a number between %d and %d: ", min, max);
            } catch (NumberFormatException e) {
                System.out.print("  Invalid input. Please enter a number: ");
            }
        }
    }
}
