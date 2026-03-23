package com.shg.view;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * DashboardView renders the main dashboard for a logged-in SHG member.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Display SHG group overview (name, member count, balance)</li>
 *   <li>Show the current member's role and permissions</li>
 *   <li>Present role-based main menu options</li>
 *   <li>Navigate the user to Finance, Advisory, Reports, and Discussions</li>
 * </ul>
 */
public class DashboardView {

    // Menu option constants returned by showMainMenu()
    public static final int MENU_FINANCE     = 1;
    public static final int MENU_REPORTS     = 2;
    public static final int MENU_ADVISORY    = 3;
    public static final int MENU_DISCUSSIONS = 4;
    public static final int MENU_ADMIN       = 5;
    public static final int MENU_LOGOUT      = 6;

    private final Scanner scanner;

    /**
     * Constructs a DashboardView bound to the supplied scanner.
     *
     * @param scanner the active {@link Scanner} reading from System.in
     */
    public DashboardView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------------------------------------------------------ //
    //  Group overview
    // ------------------------------------------------------------------ //

    /**
     * Displays the SHG group overview panel.
     *
     * @param groupName    the name of the SHG
     * @param memberCount  total number of registered members
     * @param totalBalance current pooled balance (in INR)
     * @param currentUser  the logged-in member's display name
     * @param role         the logged-in member's role
     */
    public void showGroupOverview(String groupName, int memberCount,
                                  double totalBalance,
                                  String currentUser, String role) {
        UIUtility.printHeader("DASHBOARD");
        UIUtility.printSubHeader("SHG Group Overview");
        UIUtility.printTableRow("Group Name", groupName);
        UIUtility.printTableRow("Total Members", String.valueOf(memberCount));
        UIUtility.printTableRow("Group Balance", UIUtility.formatCurrency(totalBalance));
        UIUtility.printTableRow("Logged in as", currentUser + " [" + role + "]");
        UIUtility.printSingleLine();
    }

    // ------------------------------------------------------------------ //
    //  Member roster
    // ------------------------------------------------------------------ //

    /**
     * Displays a roster of all SHG members with their roles.
     *
     * @param members a list of member descriptors in "Name (Role)" format
     */
    public void showMemberList(List<String> members) {
        UIUtility.printSubHeader("Members & Roles");
        if (members == null || members.isEmpty()) {
            UIUtility.printInfo("No members found.");
            return;
        }
        for (int i = 0; i < members.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, members.get(i));
        }
        UIUtility.printSingleLine();
    }

    // ------------------------------------------------------------------ //
    //  Main menu
    // ------------------------------------------------------------------ //

    /**
     * Displays the role-sensitive main-menu and returns the user's choice.
     *
     * <p>Admin users see an additional "Admin Panel" option (choice 5). The
     * mapping is defined by the {@code MENU_*} constants on this class.
     *
     * @param role the current user's role (case-insensitive match for "admin")
     * @return the validated menu choice integer
     */
    public int showMainMenu(String role) {
        UIUtility.printSubHeader("Main Menu");
        System.out.println("  1. Finance Module");
        System.out.println("  2. Reports");
        System.out.println("  3. Advisory");
        System.out.println("  4. Discussions");

        boolean isAdmin = "admin".equalsIgnoreCase(role);
        if (isAdmin) {
            System.out.println("  5. Admin Panel");
            System.out.println("  6. Logout");
            UIUtility.printSingleLine();
            System.out.print("  Enter your choice: ");
            return readIntInput(1, 6);
        } else {
            System.out.println("  5. Logout");
            UIUtility.printSingleLine();
            System.out.print("  Enter your choice: ");
            int raw = readIntInput(1, 5);
            // Remap "5 = Logout" for non-admin to MENU_LOGOUT constant (6)
            return (raw == 5) ? MENU_LOGOUT : raw;
        }
    }

    // ------------------------------------------------------------------ //
    //  Feedback
    // ------------------------------------------------------------------ //

    /**
     * Shows a logout confirmation message.
     *
     * @param username the name of the user who logged out
     */
    public void showLogoutMessage(String username) {
        UIUtility.printBlankLine();
        UIUtility.printInfo("Goodbye, " + username + "! You have been logged out.");
        UIUtility.printDoubleLine();
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
