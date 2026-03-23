package com.shg.view;

import java.util.List;
import java.util.Scanner;

/**
 * AdminView renders the console UI for the Admin module of the SHG Financial
 * Tracking platform.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Display pending broker verifications and allow approve/reject</li>
 *   <li>Display platform statistics</li>
 *   <li>Manage system-wide settings</li>
 * </ul>
 */
public class AdminView {

    private final Scanner scanner;

    /**
     * Constructs an AdminView bound to the supplied scanner.
     *
     * @param scanner the active {@link Scanner} reading from System.in
     */
    public AdminView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------------------------------------------------------ //
    //  Module menu
    // ------------------------------------------------------------------ //

    /**
     * Displays the Admin module menu and returns the user's selection.
     *
     * @return {@code 1} Pending Broker Verifications, {@code 2} Platform Statistics,
     *         {@code 3} Manage Settings, {@code 4} Back to Dashboard
     */
    public int showAdminMenu() {
        UIUtility.printHeader("ADMIN PANEL");
        System.out.println("  1. Pending Broker Verifications");
        System.out.println("  2. Platform Statistics");
        System.out.println("  3. Manage System Settings");
        System.out.println("  4. Back to Dashboard");
        UIUtility.printSingleLine();
        System.out.print("  Enter your choice: ");
        return readIntInput(1, 4);
    }

    // ------------------------------------------------------------------ //
    //  Broker verification
    // ------------------------------------------------------------------ //

    /**
     * Displays a list of brokers awaiting verification.
     *
     * <p>Each entry in {@code brokers} must be a four-element array:
     * {@code [id, name, firmName, submittedDate]}.
     *
     * @param brokers the list of pending broker rows
     */
    public void showPendingBrokers(List<String[]> brokers) {
        UIUtility.printSubHeader("Pending Broker Verifications");
        if (brokers == null || brokers.isEmpty()) {
            UIUtility.printInfo("No pending broker verifications.");
            return;
        }
        UIUtility.printTableHeader4("ID", "Name", "Firm", "Submitted");
        for (String[] b : brokers) {
            UIUtility.printTableRow4(
                    b.length > 0 ? b[0] : "",
                    b.length > 1 ? b[1] : "",
                    b.length > 2 ? b[2] : "",
                    b.length > 3 ? b[3] : "");
        }
        UIUtility.printSingleLine();
    }

    /**
     * Prompts the admin to select a broker by ID and choose an action.
     *
     * @return a two-element array: {@code [brokerId, action]}
     *         where action is {@code "approve"} or {@code "reject"}
     */
    public String[] showBrokerActionForm() {
        UIUtility.printSubHeader("Broker Verification Action");
        UIUtility.printInputPrompt("Enter Broker ID");
        String brokerId = scanner.nextLine().trim();

        System.out.println("  1. Approve");
        System.out.println("  2. Reject");
        UIUtility.printSingleLine();
        System.out.print("  Select action: ");
        int choice = readIntInput(1, 2);
        String action = (choice == 1) ? "approve" : "reject";
        return new String[]{brokerId, action};
    }

    /**
     * Optionally prompts the admin to enter a rejection reason.
     *
     * @return the rejection reason text, or an empty string if none given
     */
    public String showRejectionReasonForm() {
        UIUtility.printInputPrompt("Reason for rejection (optional)");
        return scanner.nextLine().trim();
    }

    // ------------------------------------------------------------------ //
    //  Platform statistics
    // ------------------------------------------------------------------ //

    /**
     * Displays high-level platform statistics.
     *
     * @param totalUsers       total registered users on the platform
     * @param totalGroups      total SHG groups registered
     * @param totalTransactions total transactions recorded
     * @param totalFunds       total funds under management (INR)
     * @param pendingBrokers   count of brokers awaiting verification
     */
    public void showPlatformStatistics(int totalUsers, int totalGroups,
                                       int totalTransactions, double totalFunds,
                                       int pendingBrokers) {
        UIUtility.printSubHeader("Platform Statistics");
        UIUtility.printTableRow("Total Users",          String.valueOf(totalUsers));
        UIUtility.printTableRow("Total SHG Groups",     String.valueOf(totalGroups));
        UIUtility.printTableRow("Total Transactions",   String.valueOf(totalTransactions));
        UIUtility.printTableRow("Total Funds Managed",  UIUtility.formatCurrency(totalFunds));
        UIUtility.printTableRow("Pending Verifications",String.valueOf(pendingBrokers));
        UIUtility.printSingleLine();
    }

    // ------------------------------------------------------------------ //
    //  System settings
    // ------------------------------------------------------------------ //

    /**
     * Displays the current system settings.
     *
     * <p>Each entry in {@code settings} must be a two-element array:
     * {@code [settingKey, settingValue]}.
     *
     * @param settings the list of key/value setting rows
     */
    public void showSystemSettings(List<String[]> settings) {
        UIUtility.printSubHeader("System Settings");
        if (settings == null || settings.isEmpty()) {
            UIUtility.printInfo("No configurable settings found.");
            return;
        }
        for (String[] s : settings) {
            UIUtility.printTableRow(
                    s.length > 0 ? s[0] : "",
                    s.length > 1 ? s[1] : "N/A");
        }
        UIUtility.printSingleLine();
    }

    /**
     * Prompts the admin to update a setting value.
     *
     * @return a two-element array: {@code [settingKey, newValue]}
     */
    public String[] showUpdateSettingForm() {
        UIUtility.printSubHeader("Update Setting");
        UIUtility.printInputPrompt("Setting Key");
        String key = scanner.nextLine().trim();
        UIUtility.printInputPrompt("New Value");
        String value = scanner.nextLine().trim();
        return new String[]{key, value};
    }

    // ------------------------------------------------------------------ //
    //  Feedback
    // ------------------------------------------------------------------ //

    /**
     * Shows a broker verification result message.
     *
     * @param brokerName the broker's display name
     * @param approved   {@code true} if the broker was approved, {@code false} if rejected
     */
    public void showBrokerVerificationResult(String brokerName, boolean approved) {
        if (approved) {
            UIUtility.printSuccess("Broker '" + brokerName + "' has been approved.");
        } else {
            UIUtility.printInfo("Broker '" + brokerName + "' has been rejected.");
        }
    }

    /**
     * Displays a generic error message within the Admin module.
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
