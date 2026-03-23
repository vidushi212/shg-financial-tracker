package com.shg.view;

import java.util.Arrays;
import java.util.Scanner;

/**
 * LoginView handles all console interactions for user authentication and
 * registration in the SHG Financial Tracking platform.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Display login / registration menu</li>
 *   <li>Collect and validate username and password</li>
 *   <li>Show role selection during registration</li>
 *   <li>Display error and success messages</li>
 * </ul>
 */
public class LoginView {

    /** Available roles that a new user may choose during registration. */
    private static final String[] ROLES = {
        "Member",
        "President",
        "Secretary",
        "Treasurer",
        "Broker",
        "Admin"
    };

    private final Scanner scanner;

    /**
     * Constructs a LoginView that reads from the supplied scanner.
     *
     * @param scanner the active {@link Scanner} bound to System.in
     */
    public LoginView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------------------------------------------------------ //
    //  Top-level menu
    // ------------------------------------------------------------------ //

    /**
     * Displays the welcome screen and the top-level authentication menu.
     *
     * @return the user's selection: {@code 1} = Login, {@code 2} = Register,
     *         {@code 3} = Exit
     */
    public int showMainMenu() {
        UIUtility.printHeader("SHG Financial Tracking & Advisory Platform");
        System.out.println();
        UIUtility.printInfo("Welcome! Please select an option:");
        System.out.println();
        System.out.println("  1. Login");
        System.out.println("  2. Register");
        System.out.println("  3. Exit");
        UIUtility.printSingleLine();
        System.out.print("  Enter your choice: ");
        return readIntInput(1, 3);
    }

    // ------------------------------------------------------------------ //
    //  Login flow
    // ------------------------------------------------------------------ //

    /**
     * Renders the login form and collects credentials from the user.
     *
     * @return a two-element array where index {@code 0} is the username and
     *         index {@code 1} is the password
     */
    public String[] showLoginForm() {
        UIUtility.printHeader("LOGIN");
        UIUtility.printInputPrompt("Username");
        String username = scanner.nextLine().trim();
        UIUtility.printInputPrompt("Password");
        String password = scanner.nextLine().trim();
        return new String[]{username, password};
    }

    // ------------------------------------------------------------------ //
    //  Registration flow
    // ------------------------------------------------------------------ //

    /**
     * Renders the registration form, collects all required fields, and
     * returns the collected data.
     *
     * @return a three-element array: {@code [username, password, role]}
     */
    public String[] showRegistrationForm() {
        UIUtility.printHeader("REGISTER NEW USER");

        UIUtility.printInputPrompt("Full Name");
        String name = scanner.nextLine().trim();

        UIUtility.printInputPrompt("Username");
        String username = scanner.nextLine().trim();

        String password = "";
        while (true) {
            UIUtility.printInputPrompt("Password (min 6 chars)");
            password = scanner.nextLine().trim();
            if (password.length() >= 6) {
                break;
            }
            UIUtility.printError("Password must be at least 6 characters.");
        }

        UIUtility.printInputPrompt("Confirm Password");
        String confirm = scanner.nextLine().trim();
        if (!password.equals(confirm)) {
            UIUtility.printError("Passwords do not match. Registration cancelled.");
            return null;
        }

        String role = showRoleSelection();
        return new String[]{name, username, password, role};
    }

    /**
     * Displays the role-selection sub-menu and returns the chosen role label.
     *
     * @return the selected role string
     */
    public String showRoleSelection() {
        UIUtility.printSubHeader("Select Your Role");
        for (int i = 0; i < ROLES.length; i++) {
            System.out.printf("  %2d. %s%n", i + 1, ROLES[i]);
        }
        UIUtility.printSingleLine();
        System.out.print("  Enter role number: ");
        int choice = readIntInput(1, ROLES.length);
        return ROLES[choice - 1];
    }

    // ------------------------------------------------------------------ //
    //  Feedback methods
    // ------------------------------------------------------------------ //

    /**
     * Displays a login-success confirmation with the user's role.
     *
     * @param username the authenticated user's display name
     * @param role     the role assigned to that user
     */
    public void showLoginSuccess(String username, String role) {
        UIUtility.printSuccess("Welcome back, " + username + "! [Role: " + role + "]");
        UIUtility.printBlankLine();
    }

    /**
     * Displays a registration-success confirmation.
     *
     * @param username the newly registered user's name
     */
    public void showRegistrationSuccess(String username) {
        UIUtility.printSuccess("Account created successfully for '" + username + "'. You can now log in.");
        UIUtility.printBlankLine();
    }

    /**
     * Displays a generic error message on the login/registration screen.
     *
     * @param message the error text
     */
    public void showError(String message) {
        UIUtility.printError(message);
    }

    // ------------------------------------------------------------------ //
    //  Input helpers
    // ------------------------------------------------------------------ //

    /**
     * Reads an integer from stdin, re-prompting on invalid input, until the
     * value falls within [{@code min}, {@code max}].
     *
     * @param min minimum accepted value (inclusive)
     * @param max maximum accepted value (inclusive)
     * @return the validated integer
     */
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
