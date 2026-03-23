package com.shg.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * ApplicationNavigator is the main entry point for the SHG Financial Tracking
 * &amp; Advisory Platform console application.
 *
 * <p>It wires together all view classes, manages the authentication lifecycle,
 * and routes the logged-in user to the correct module based on their role.
 *
 * <p>This class intentionally uses in-memory demo data so that the UI layer can
 * be exercised stand-alone without requiring a persistence or controller layer.
 * Replace the stub data and stub authentication logic with real controller
 * calls when integrating with the full MVC stack.
 */
public class ApplicationNavigator {

    // ------------------------------------------------------------------ //
    //  State
    // ------------------------------------------------------------------ //

    private final Scanner scanner = new Scanner(System.in);

    /** Currently authenticated user's display name (null = not logged in). */
    private String currentUser  = null;
    /** Currently authenticated user's role (null = not logged in). */
    private String currentRole  = null;

    // ------------------------------------------------------------------ //
    //  View instances
    // ------------------------------------------------------------------ //

    private final LoginView      loginView;
    private final DashboardView  dashboardView;
    private final FinanceView    financeView;
    private final ReportView     reportView;
    private final AdvisoryView   advisoryView;
    private final DiscussionView discussionView;
    private final AdminView      adminView;

    // ------------------------------------------------------------------ //
    //  Constructor
    // ------------------------------------------------------------------ //

    /** Creates all view instances and binds them to the shared scanner. */
    public ApplicationNavigator() {
        loginView      = new LoginView(scanner);
        dashboardView  = new DashboardView(scanner);
        financeView    = new FinanceView(scanner);
        reportView     = new ReportView(scanner);
        advisoryView   = new AdvisoryView(scanner);
        discussionView = new DiscussionView(scanner);
        adminView      = new AdminView(scanner);
    }

    // ------------------------------------------------------------------ //
    //  Entry point
    // ------------------------------------------------------------------ //

    /**
     * Application entry point.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        new ApplicationNavigator().run();
    }

    /**
     * Starts the application loop. The loop continues until the user
     * explicitly exits from the authentication screen.
     */
    public void run() {
        boolean exit = false;
        while (!exit) {
            int choice = loginView.showMainMenu();
            switch (choice) {
                case 1:
                    handleLogin();
                    break;
                case 2:
                    handleRegistration();
                    break;
                case 3:
                    exit = true;
                    UIUtility.printInfo("Thank you for using SHG Financial Tracker. Goodbye!");
                    UIUtility.printDoubleLine();
                    break;
                default:
                    loginView.showError("Invalid selection.");
            }
        }
        scanner.close();
    }

    // ------------------------------------------------------------------ //
    //  Authentication flow
    // ------------------------------------------------------------------ //

    /** Handles the login interaction. Navigates to the dashboard on success. */
    private void handleLogin() {
        String[] credentials = loginView.showLoginForm();
        // --- Stub authentication: accept any non-blank username/password ---
        String username = credentials[0];
        String password = credentials[1];

        if (username.isEmpty() || password.isEmpty()) {
            loginView.showError("Username and password cannot be empty.");
            return;
        }

        // Assign a demo role based on username prefix for demo purposes
        String role = detectDemoRole(username);
        currentUser = username;
        currentRole = role;

        loginView.showLoginSuccess(username, role);
        runDashboardLoop();
    }

    /** Handles the registration interaction. */
    private void handleRegistration() {
        String[] data = loginView.showRegistrationForm();
        if (data == null) {
            return; // cancelled due to password mismatch
        }
        String name     = data[0];
        String username = data[1];
        // data[2] = password, data[3] = role
        loginView.showRegistrationSuccess(username);
        UIUtility.printInfo("Registration stored. Please log in.");
    }

    // ------------------------------------------------------------------ //
    //  Dashboard loop
    // ------------------------------------------------------------------ //

    /**
     * The post-login loop that renders the dashboard and routes to modules.
     * Returns when the user chooses Logout.
     */
    private void runDashboardLoop() {
        boolean loggedIn = true;
        while (loggedIn) {
            dashboardView.showGroupOverview(
                    "Shakti Mahila Sangha",
                    12,
                    85_000.00,
                    currentUser,
                    currentRole);

            dashboardView.showMemberList(demoMemberList());

            int choice = dashboardView.showMainMenu(currentRole);
            switch (choice) {
                case DashboardView.MENU_FINANCE:
                    runFinanceLoop();
                    break;
                case DashboardView.MENU_REPORTS:
                    runReportLoop();
                    break;
                case DashboardView.MENU_ADVISORY:
                    runAdvisoryLoop();
                    break;
                case DashboardView.MENU_DISCUSSIONS:
                    runDiscussionLoop();
                    break;
                case DashboardView.MENU_ADMIN:
                    if ("admin".equalsIgnoreCase(currentRole)) {
                        runAdminLoop();
                    } else {
                        UIUtility.printError("Access denied. Admin privileges required.");
                    }
                    break;
                case DashboardView.MENU_LOGOUT:
                    dashboardView.showLogoutMessage(currentUser);
                    currentUser = null;
                    currentRole = null;
                    loggedIn = false;
                    break;
                default:
                    UIUtility.printError("Invalid selection.");
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Finance module loop
    // ------------------------------------------------------------------ //

    private void runFinanceLoop() {
        boolean running = true;
        while (running) {
            int choice = financeView.showFinanceMenu();
            switch (choice) {
                case 1: // Record transaction
                    int typeChoice = financeView.showTransactionTypeMenu();
                    String typeName = typeLabel(typeChoice);
                    String[] txData = financeView.showTransactionForm(typeName);
                    double amount = Double.parseDouble(txData[0]);
                    financeView.showTransactionRecorded(typeName, amount);
                    UIUtility.pauseForInput(scanner);
                    break;
                case 2: // View history
                    int filterChoice = financeView.showFilterMenu();
                    if (filterChoice == 5) {
                        String[] dates = financeView.showDateRangeInput();
                        UIUtility.printInfo("Showing transactions from " + dates[0] + " to " + dates[1]);
                    }
                    financeView.showTransactionHistory(demoTransactions());
                    UIUtility.pauseForInput(scanner);
                    break;
                case 3: // Balance
                    financeView.showBalance(52_000, 20_000, 8_500, 23_500);
                    UIUtility.pauseForInput(scanner);
                    break;
                case 4: // Back
                    running = false;
                    break;
                default:
                    financeView.showError("Invalid selection.");
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Reports module loop
    // ------------------------------------------------------------------ //

    private void runReportLoop() {
        boolean running = true;
        while (running) {
            int choice = reportView.showReportMenu();
            switch (choice) {
                case 1: // Monthly summary
                    String[] my = reportView.showMonthYearInput();
                    String monthLabel = monthName(Integer.parseInt(my[0])) + " " + my[1];
                    reportView.showMonthlySummary(monthLabel, 18_000, 7_000, 3_200, 7_800);
                    UIUtility.pauseForInput(scanner);
                    break;
                case 2: // Comparative
                    reportView.showComparativeReport(demoComparativeData());
                    UIUtility.pauseForInput(scanner);
                    break;
                case 3: // Investment comparison
                    reportView.showInvestmentComparison(demoInvestmentPlans());
                    UIUtility.pauseForInput(scanner);
                    break;
                case 4: // Back
                    running = false;
                    break;
                default:
                    UIUtility.printError("Invalid selection.");
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Advisory module loop
    // ------------------------------------------------------------------ //

    private void runAdvisoryLoop() {
        boolean running = true;
        while (running) {
            int choice = advisoryView.showAdvisoryMenu();
            switch (choice) {
                case 1: // Investment plans
                    advisoryView.showInvestmentPlans(demoBrokerPlans());
                    UIUtility.pauseForInput(scanner);
                    break;
                case 2: // Government schemes
                    advisoryView.showGovernmentSchemes(demoGovSchemes());
                    UIUtility.pauseForInput(scanner);
                    break;
                case 3: // AI recommendations
                    advisoryView.showAIRecommendations(demoRecommendations());
                    UIUtility.pauseForInput(scanner);
                    break;
                case 4: // Discuss recommendation
                    List<String[]> recs = demoRecommendations();
                    advisoryView.showAIRecommendations(recs);
                    int sel = advisoryView.showRecommendationSelection(recs.size());
                    UIUtility.printInfo("Opening discussion for: " + recs.get(sel - 1)[0]);
                    UIUtility.pauseForInput(scanner);
                    break;
                case 5: // Back
                    running = false;
                    break;
                default:
                    UIUtility.printError("Invalid selection.");
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Discussion module loop
    // ------------------------------------------------------------------ //

    private void runDiscussionLoop() {
        boolean running = true;
        while (running) {
            int choice = discussionView.showDiscussionMenu();
            switch (choice) {
                case 1: // View all
                    discussionView.showDiscussionList(demoDiscussions());
                    String id = discussionView.showSelectDiscussion();
                    discussionView.showDiscussionThread(
                            "Monthly Savings Target",
                            "Priya S.",
                            "01-03-2024",
                            demoComments());
                    String comment = discussionView.showAddCommentForm();
                    if (!comment.isEmpty()) {
                        discussionView.showCommentPosted();
                    }
                    UIUtility.pauseForInput(scanner);
                    break;
                case 2: // New discussion
                    String[] nd = discussionView.showNewDiscussionForm();
                    if (!nd[0].isEmpty()) {
                        discussionView.showDiscussionCreated(nd[0]);
                    }
                    UIUtility.pauseForInput(scanner);
                    break;
                case 3: // Recommendation discussions
                    discussionView.showRecommendationDiscussions(demoRecDiscussions());
                    UIUtility.pauseForInput(scanner);
                    break;
                case 4: // Back
                    running = false;
                    break;
                default:
                    discussionView.showError("Invalid selection.");
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Admin module loop
    // ------------------------------------------------------------------ //

    private void runAdminLoop() {
        boolean running = true;
        while (running) {
            int choice = adminView.showAdminMenu();
            switch (choice) {
                case 1: // Pending brokers
                    adminView.showPendingBrokers(demoPendingBrokers());
                    String[] action = adminView.showBrokerActionForm();
                    boolean approved = "approve".equals(action[1]);
                    if (!approved) {
                        adminView.showRejectionReasonForm();
                    }
                    adminView.showBrokerVerificationResult("Demo Broker", approved);
                    UIUtility.pauseForInput(scanner);
                    break;
                case 2: // Statistics
                    adminView.showPlatformStatistics(48, 5, 320, 4_25_000, 3);
                    UIUtility.pauseForInput(scanner);
                    break;
                case 3: // Settings
                    adminView.showSystemSettings(demoSettings());
                    String[] update = adminView.showUpdateSettingForm();
                    UIUtility.printSuccess("Setting '" + update[0] + "' updated to '" + update[1] + "'.");
                    UIUtility.pauseForInput(scanner);
                    break;
                case 4: // Back
                    running = false;
                    break;
                default:
                    adminView.showError("Invalid selection.");
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Stub / demo data helpers
    // ------------------------------------------------------------------ //

    private String detectDemoRole(String username) {
        if (username.toLowerCase().startsWith("admin"))    return "Admin";
        if (username.toLowerCase().startsWith("broker"))   return "Broker";
        if (username.toLowerCase().startsWith("president"))return "President";
        if (username.toLowerCase().startsWith("treasurer"))return "Treasurer";
        return "Member";
    }

    private String typeLabel(int type) {
        switch (type) {
            case FinanceView.TYPE_SAVINGS: return "Savings";
            case FinanceView.TYPE_LOAN:    return "Loan";
            case FinanceView.TYPE_EXPENSE: return "Expense";
            default: return "Unknown";
        }
    }

    private String monthName(int month) {
        String[] names = {"January","February","March","April","May","June",
                          "July","August","September","October","November","December"};
        if (month >= 1 && month <= 12) return names[month - 1];
        return "Unknown";
    }

    private List<String> demoMemberList() {
        return Arrays.asList(
                "Priya Sharma       (President)",
                "Lakshmi Devi       (Treasurer)",
                "Meena Patel        (Secretary)",
                "Anita Rao          (Member)",
                "Savita Kumari      (Member)");
    }

    private List<String[]> demoTransactions() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"01-03-2024", "Savings",  "5000.00", "Monthly savings"});
        list.add(new String[]{"05-03-2024", "Loan",     "10000.00","Business loan - Priya"});
        list.add(new String[]{"10-03-2024", "Expense",  "1200.00", "Meeting expenses"});
        list.add(new String[]{"15-03-2024", "Savings",  "5000.00", "Monthly savings"});
        return list;
    }

    private List<String[]> demoComparativeData() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Jan 2024", "15000", "8000", "2500"});
        list.add(new String[]{"Feb 2024", "16500", "9000", "3000"});
        list.add(new String[]{"Mar 2024", "18000", "7000", "3200"});
        return list;
    }

    private List<String[]> demoInvestmentPlans() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"SBI Fixed Deposit",  "8.5",  "Low",    "1 Year"});
        list.add(new String[]{"Post Office MIS",    "7.4",  "Low",    "5 Years"});
        list.add(new String[]{"Sukanya Samriddhi",  "8.2",  "Low",    "21 Years"});
        return list;
    }

    private List<String[]> demoBrokerPlans() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"SBI SHG Loan",   "SBI Bank",    "9.5",  "Low",    "10000"});
        list.add(new String[]{"Micro Finance",  "Grameen Bank","12.0", "Medium", "5000"});
        return list;
    }

    private List<String[]> demoGovSchemes() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"DAY-NRLM",          "Ministry of Rural Development",
                              "Subsidised credit & capacity building", "Rural SHGs"});
        list.add(new String[]{"PM Jan Dhan Yojana","Ministry of Finance",
                              "Zero-balance savings account",          "All citizens"});
        return list;
    }

    private List<String[]> demoRecommendations() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Increase Monthly Savings",
                "Group balance is below the 6-month target threshold.", "High"});
        list.add(new String[]{"Consider Fixed Deposit",
                "Idle funds can earn higher returns in SBI FD.",         "Medium"});
        return list;
    }

    private List<String[]> demoDiscussions() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"D001", "Monthly Savings Target",   "Priya S.",  "3"});
        list.add(new String[]{"D002", "Loan Repayment Schedule",  "Lakshmi D.","1"});
        return list;
    }

    private List<String[]> demoComments() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Lakshmi D.", "01-03-2024 10:15", "I agree we should increase it by ₹500."});
        list.add(new String[]{"Meena P.",   "02-03-2024 09:30", "Let's discuss in the next meeting."});
        return list;
    }

    private List<String[]> demoRecDiscussions() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Increase Monthly Savings", "Priya S.",  "2"});
        list.add(new String[]{"Consider Fixed Deposit",   "Anita R.",  "1"});
        return list;
    }

    private List<String[]> demoPendingBrokers() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"B001", "Raj Kumar",   "RK Investments", "10-03-2024"});
        list.add(new String[]{"B002", "Sunita Joshi","SJ Finance",     "12-03-2024"});
        return list;
    }

    private List<String[]> demoSettings() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Max Loan Amount (₹)",    "50000"});
        list.add(new String[]{"Min Savings Per Month",  "500"});
        list.add(new String[]{"Loan Interest Rate (%)", "12"});
        return list;
    }
}
