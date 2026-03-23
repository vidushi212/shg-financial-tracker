package com.shg.view;

import java.util.List;
import java.util.Scanner;

/**
 * AdvisoryView renders the console UI for the Advisory module of the SHG
 * Financial Tracking platform.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Display available investment plans from bank brokers</li>
 *   <li>Display government schemes</li>
 *   <li>Show AI-generated recommendations</li>
 *   <li>Allow members to view and discuss recommendations</li>
 * </ul>
 */
public class AdvisoryView {

    private final Scanner scanner;

    /**
     * Constructs an AdvisoryView bound to the supplied scanner.
     *
     * @param scanner the active {@link Scanner} reading from System.in
     */
    public AdvisoryView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------------------------------------------------------ //
    //  Module menu
    // ------------------------------------------------------------------ //

    /**
     * Displays the Advisory module menu and returns the user's selection.
     *
     * @return {@code 1} Investment Plans, {@code 2} Government Schemes,
     *         {@code 3} AI Recommendations, {@code 4} Discuss a Recommendation,
     *         {@code 5} Back to Dashboard
     */
    public int showAdvisoryMenu() {
        UIUtility.printHeader("ADVISORY MODULE");
        System.out.println("  1. View Investment Plans (Broker Listings)");
        System.out.println("  2. Government Schemes");
        System.out.println("  3. AI-Generated Recommendations");
        System.out.println("  4. Discuss a Recommendation");
        System.out.println("  5. Back to Dashboard");
        UIUtility.printSingleLine();
        System.out.print("  Enter your choice: ");
        return readIntInput(1, 5);
    }

    // ------------------------------------------------------------------ //
    //  Investment plans
    // ------------------------------------------------------------------ //

    /**
     * Displays investment plans listed by verified brokers.
     *
     * <p>Each entry in {@code plans} must be a five-element array:
     * {@code [planName, brokerName, returnRate, riskLevel, minInvestment]}.
     *
     * @param plans the list of broker-listed investment plans
     */
    public void showInvestmentPlans(List<String[]> plans) {
        UIUtility.printSubHeader("Investment Plans from Brokers");
        if (plans == null || plans.isEmpty()) {
            UIUtility.printInfo("No investment plans available at this time.");
            return;
        }

        for (int i = 0; i < plans.size(); i++) {
            String[] p = plans.get(i);
            System.out.println("  [" + (i + 1) + "] " + (p.length > 0 ? p[0] : ""));
            UIUtility.printTableRow("  Broker",          p.length > 1 ? p[1] : "N/A");
            UIUtility.printTableRow("  Expected Return", (p.length > 2 ? p[2] : "N/A") + "%");
            UIUtility.printTableRow("  Risk Level",      p.length > 3 ? p[3] : "N/A");
            UIUtility.printTableRow("  Min Investment",  UIUtility.formatCurrency(
                    parseDouble(p.length > 4 ? p[4] : "0")));
            UIUtility.printSingleLine();
        }
    }

    // ------------------------------------------------------------------ //
    //  Government schemes
    // ------------------------------------------------------------------ //

    /**
     * Displays a list of government financial schemes relevant to SHGs.
     *
     * <p>Each entry in {@code schemes} must be a four-element array:
     * {@code [schemeName, authority, benefit, eligibility]}.
     *
     * @param schemes the list of government scheme data rows
     */
    public void showGovernmentSchemes(List<String[]> schemes) {
        UIUtility.printSubHeader("Government Schemes for SHGs");
        if (schemes == null || schemes.isEmpty()) {
            UIUtility.printInfo("No government schemes available.");
            return;
        }

        for (int i = 0; i < schemes.size(); i++) {
            String[] s = schemes.get(i);
            System.out.println("  [" + (i + 1) + "] " + (s.length > 0 ? s[0] : ""));
            UIUtility.printTableRow("  Authority",   s.length > 1 ? s[1] : "N/A");
            UIUtility.printTableRow("  Benefit",     s.length > 2 ? s[2] : "N/A");
            UIUtility.printTableRow("  Eligibility", s.length > 3 ? s[3] : "N/A");
            UIUtility.printSingleLine();
        }
    }

    // ------------------------------------------------------------------ //
    //  AI recommendations
    // ------------------------------------------------------------------ //

    /**
     * Displays AI-generated investment recommendations for the SHG.
     *
     * <p>Each entry in {@code recommendations} must be a three-element array:
     * {@code [title, rationale, priority]}.
     *
     * @param recommendations the list of AI-generated recommendation rows
     */
    public void showAIRecommendations(List<String[]> recommendations) {
        UIUtility.printSubHeader("AI-Generated Recommendations");
        if (recommendations == null || recommendations.isEmpty()) {
            UIUtility.printInfo("No recommendations generated yet. Please check back later.");
            return;
        }

        for (int i = 0; i < recommendations.size(); i++) {
            String[] r = recommendations.get(i);
            System.out.println("  [" + (i + 1) + "] " + (r.length > 0 ? r[0] : ""));
            UIUtility.printTableRow("  Rationale", r.length > 1 ? r[1] : "N/A");
            UIUtility.printTableRow("  Priority",  r.length > 2 ? r[2] : "N/A");
            UIUtility.printSingleLine();
        }
    }

    // ------------------------------------------------------------------ //
    //  Discuss recommendation
    // ------------------------------------------------------------------ //

    /**
     * Prompts the user to select a recommendation by its list number.
     *
     * @param maxIndex the highest valid selection number (1-based)
     * @return the user's 1-based selection
     */
    public int showRecommendationSelection(int maxIndex) {
        UIUtility.printSubHeader("Select Recommendation to Discuss");
        System.out.printf("  Enter recommendation number (1-%d): ", maxIndex);
        return readIntInput(1, maxIndex);
    }

    // ------------------------------------------------------------------ //
    //  Feedback
    // ------------------------------------------------------------------ //

    /**
     * Displays a generic error message within the Advisory module.
     *
     * @param message the error description
     */
    public void showError(String message) {
        UIUtility.printError(message);
    }

    // ------------------------------------------------------------------ //
    //  Private helpers
    // ------------------------------------------------------------------ //

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

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
