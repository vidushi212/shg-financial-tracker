package com.shg.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * FinanceView provides the console UI for the Finance module of the SHG
 * Financial Tracking platform.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Record new transactions (savings, loans, expenses)</li>
 *   <li>View transaction history</li>
 *   <li>Display current account balance</li>
 *   <li>Allow filtering of transactions by type or date range</li>
 * </ul>
 */
public class FinanceView {

    /** Transaction type constants returned by {@link #showTransactionTypeMenu()}. */
    public static final int TYPE_SAVINGS  = 1;
    public static final int TYPE_LOAN     = 2;
    public static final int TYPE_EXPENSE  = 3;

    private final Scanner scanner;

    /**
     * Constructs a FinanceView bound to the supplied scanner.
     *
     * @param scanner the active {@link Scanner} reading from System.in
     */
    public FinanceView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------------------------------------------------------ //
    //  Module menu
    // ------------------------------------------------------------------ //

    /**
     * Displays the Finance module menu and returns the user's selection.
     *
     * @return {@code 1} Record Transaction, {@code 2} Transaction History,
     *         {@code 3} View Balance, {@code 4} Back to Dashboard
     */
    public int showFinanceMenu() {
        UIUtility.printHeader("FINANCE MODULE");
        System.out.println("  1. Record New Transaction");
        System.out.println("  2. View Transaction History");
        System.out.println("  3. View Account Balance");
        System.out.println("  4. Back to Dashboard");
        UIUtility.printSingleLine();
        System.out.print("  Enter your choice: ");
        return readIntInput(1, 4);
    }

    // ------------------------------------------------------------------ //
    //  Record transaction
    // ------------------------------------------------------------------ //

    /**
     * Prompts the user to choose a transaction type.
     *
     * @return {@link #TYPE_SAVINGS}, {@link #TYPE_LOAN}, or {@link #TYPE_EXPENSE}
     */
    public int showTransactionTypeMenu() {
        UIUtility.printSubHeader("Transaction Type");
        System.out.println("  1. Savings");
        System.out.println("  2. Loan");
        System.out.println("  3. Expense");
        UIUtility.printSingleLine();
        System.out.print("  Select type: ");
        return readIntInput(1, 3);
    }

    /**
     * Collects the details required to record a new transaction.
     *
     * @param type the transaction type label (e.g. "Savings", "Loan", "Expense")
     * @return a three-element array: {@code [amount, description, date (dd-MM-yyyy)]}
     */
    public String[] showTransactionForm(String type) {
        UIUtility.printSubHeader("Record " + type + " Transaction");

        double amount = 0;
        while (true) {
            UIUtility.printInputPrompt("Amount (₹)");
            try {
                amount = Double.parseDouble(scanner.nextLine().trim());
                if (amount <= 0) {
                    UIUtility.printError("Amount must be greater than zero.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                UIUtility.printError("Please enter a valid numeric amount.");
            }
        }

        UIUtility.printInputPrompt("Description");
        String description = scanner.nextLine().trim();

        UIUtility.printInputPrompt("Date (dd-MM-yyyy) [Enter for today]");
        String dateInput = scanner.nextLine().trim();
        if (dateInput.isEmpty()) {
            dateInput = UIUtility.today();
        }

        return new String[]{String.valueOf(amount), description, dateInput};
    }

    // ------------------------------------------------------------------ //
    //  Transaction history
    // ------------------------------------------------------------------ //

    /**
     * Displays the transaction history filter menu.
     *
     * @return {@code 1} All, {@code 2} Savings, {@code 3} Loans,
     *         {@code 4} Expenses, {@code 5} Filter by Date Range
     */
    public int showFilterMenu() {
        UIUtility.printSubHeader("Filter Transactions");
        System.out.println("  1. All Transactions");
        System.out.println("  2. Savings Only");
        System.out.println("  3. Loans Only");
        System.out.println("  4. Expenses Only");
        System.out.println("  5. Filter by Date Range");
        UIUtility.printSingleLine();
        System.out.print("  Select filter: ");
        return readIntInput(1, 5);
    }

    /**
     * Collects a start and end date for a date-range filter.
     *
     * @return a two-element array: {@code [startDate, endDate]} (dd-MM-yyyy)
     */
    public String[] showDateRangeInput() {
        UIUtility.printSubHeader("Date Range Filter");
        UIUtility.printInputPrompt("Start Date (dd-MM-yyyy)");
        String start = scanner.nextLine().trim();
        UIUtility.printInputPrompt("End Date   (dd-MM-yyyy)");
        String end = scanner.nextLine().trim();
        return new String[]{start, end};
    }

    /**
     * Renders the transaction history table.
     *
     * <p>Each entry in {@code transactions} must be a four-element array:
     * {@code [date, type, amount, description]}.
     *
     * @param transactions list of transaction data rows
     */
    public void showTransactionHistory(List<String[]> transactions) {
        UIUtility.printSubHeader("Transaction History");
        if (transactions == null || transactions.isEmpty()) {
            UIUtility.printInfo("No transactions found.");
            return;
        }
        UIUtility.printTableHeader4("Date", "Type", "Amount (₹)", "Description");
        for (String[] row : transactions) {
            UIUtility.printTableRow4(
                    row.length > 0 ? row[0] : "",
                    row.length > 1 ? row[1] : "",
                    row.length > 2 ? row[2] : "",
                    row.length > 3 ? row[3] : "");
        }
        UIUtility.printSingleLine();
    }

    // ------------------------------------------------------------------ //
    //  Balance display
    // ------------------------------------------------------------------ //

    /**
     * Displays a summary of the current account balance.
     *
     * @param totalSavings  cumulative savings amount
     * @param totalLoans    outstanding loan amount disbursed
     * @param totalExpenses cumulative expenses
     * @param netBalance    net balance after all transactions
     */
    public void showBalance(double totalSavings, double totalLoans,
                            double totalExpenses, double netBalance) {
        UIUtility.printSubHeader("Account Balance Summary");
        UIUtility.printTableRow("Total Savings",  UIUtility.formatCurrency(totalSavings));
        UIUtility.printTableRow("Total Loans",    UIUtility.formatCurrency(totalLoans));
        UIUtility.printTableRow("Total Expenses", UIUtility.formatCurrency(totalExpenses));
        UIUtility.printSingleLine();
        UIUtility.printTableRow("Net Balance", UIUtility.formatCurrency(netBalance));
        UIUtility.printSingleLine();
    }

    // ------------------------------------------------------------------ //
    //  Feedback
    // ------------------------------------------------------------------ //

    /**
     * Shows a transaction-recorded confirmation message.
     *
     * @param type   the type of the recorded transaction
     * @param amount the transaction amount
     */
    public void showTransactionRecorded(String type, double amount) {
        UIUtility.printSuccess(type + " transaction of "
                + UIUtility.formatCurrency(amount) + " recorded successfully.");
    }

    /**
     * Shows a generic error message within the Finance module.
     *
     * @param message the error description
     */
    public void showError(String message) {
        UIUtility.printError(message);
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
