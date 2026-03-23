package com.shg.view;

import java.util.List;
import java.util.Scanner;

/**
 * DiscussionView renders the console UI for the Discussions module of the
 * SHG Financial Tracking platform.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Display ongoing discussions and their threads</li>
 *   <li>Allow members to post comments</li>
 *   <li>Show discussion threads with nested replies</li>
 *   <li>Display recommendation-related discussions</li>
 * </ul>
 */
public class DiscussionView {

    private final Scanner scanner;

    /**
     * Constructs a DiscussionView bound to the supplied scanner.
     *
     * @param scanner the active {@link Scanner} reading from System.in
     */
    public DiscussionView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------------------------------------------------------ //
    //  Module menu
    // ------------------------------------------------------------------ //

    /**
     * Displays the Discussions module menu and returns the user's selection.
     *
     * @return {@code 1} View All Discussions, {@code 2} Start New Discussion,
     *         {@code 3} View Recommendation Discussions, {@code 4} Back to Dashboard
     */
    public int showDiscussionMenu() {
        UIUtility.printHeader("DISCUSSIONS MODULE");
        System.out.println("  1. View All Discussions");
        System.out.println("  2. Start New Discussion");
        System.out.println("  3. View Recommendation Discussions");
        System.out.println("  4. Back to Dashboard");
        UIUtility.printSingleLine();
        System.out.print("  Enter your choice: ");
        return readIntInput(1, 4);
    }

    // ------------------------------------------------------------------ //
    //  Discussion list
    // ------------------------------------------------------------------ //

    /**
     * Displays a list of ongoing discussions.
     *
     * <p>Each entry in {@code discussions} must be a four-element array:
     * {@code [id, title, author, commentCount]}.
     *
     * @param discussions the list of discussion summary rows
     */
    public void showDiscussionList(List<String[]> discussions) {
        UIUtility.printSubHeader("Ongoing Discussions");
        if (discussions == null || discussions.isEmpty()) {
            UIUtility.printInfo("No discussions found. Be the first to start one!");
            return;
        }
        UIUtility.printTableHeader4("ID", "Title", "Author", "Comments");
        for (String[] d : discussions) {
            UIUtility.printTableRow4(
                    d.length > 0 ? d[0] : "",
                    d.length > 1 ? d[1] : "",
                    d.length > 2 ? d[2] : "",
                    d.length > 3 ? d[3] : "0");
        }
        UIUtility.printSingleLine();
    }

    // ------------------------------------------------------------------ //
    //  Single discussion thread
    // ------------------------------------------------------------------ //

    /**
     * Displays a full discussion thread including all posted comments.
     *
     * <p>Each entry in {@code comments} must be a three-element array:
     * {@code [author, timestamp, text]}.
     *
     * @param title    the discussion title
     * @param author   the user who started the discussion
     * @param date     the date the discussion was started (dd-MM-yyyy)
     * @param comments the ordered list of comment rows
     */
    public void showDiscussionThread(String title, String author,
                                     String date, List<String[]> comments) {
        UIUtility.printSubHeader("Discussion: " + title);
        UIUtility.printTableRow("Started by", author);
        UIUtility.printTableRow("Date",        date);
        UIUtility.printSingleLine();

        if (comments == null || comments.isEmpty()) {
            UIUtility.printInfo("No comments yet. Be the first to reply!");
        } else {
            System.out.println("  Comments:");
            for (int i = 0; i < comments.size(); i++) {
                String[] c = comments.get(i);
                System.out.printf("%n  [%d] %s  (%s)%n",
                        i + 1,
                        c.length > 0 ? c[0] : "Unknown",
                        c.length > 1 ? c[1] : "");
                System.out.println("       " + (c.length > 2 ? c[2] : ""));
            }
        }
        UIUtility.printSingleLine();
    }

    // ------------------------------------------------------------------ //
    //  Post / start discussion
    // ------------------------------------------------------------------ //

    /**
     * Prompts the user to enter a new discussion title and opening message.
     *
     * @return a two-element array: {@code [title, openingMessage]}
     */
    public String[] showNewDiscussionForm() {
        UIUtility.printSubHeader("Start New Discussion");
        UIUtility.printInputPrompt("Title");
        String title = scanner.nextLine().trim();
        UIUtility.printInputPrompt("Opening Message");
        String message = scanner.nextLine().trim();
        return new String[]{title, message};
    }

    /**
     * Prompts the user to enter a comment to post in the selected discussion.
     *
     * @return the comment text, or an empty string if the user cancels
     */
    public String showAddCommentForm() {
        UIUtility.printSubHeader("Add Comment");
        UIUtility.printInputPrompt("Your comment (leave blank to cancel)");
        return scanner.nextLine().trim();
    }

    /**
     * Prompts the user to select a discussion by its ID.
     *
     * @return the entered discussion ID string
     */
    public String showSelectDiscussion() {
        UIUtility.printSubHeader("Open Discussion");
        UIUtility.printInputPrompt("Enter Discussion ID");
        return scanner.nextLine().trim();
    }

    // ------------------------------------------------------------------ //
    //  Recommendation discussions
    // ------------------------------------------------------------------ //

    /**
     * Displays discussions linked to specific advisory recommendations.
     *
     * <p>Each entry in {@code discussions} must be a three-element array:
     * {@code [recommendation, author, commentCount]}.
     *
     * @param discussions the list of recommendation-linked discussion rows
     */
    public void showRecommendationDiscussions(List<String[]> discussions) {
        UIUtility.printSubHeader("Recommendation Discussions");
        if (discussions == null || discussions.isEmpty()) {
            UIUtility.printInfo("No recommendation discussions found.");
            return;
        }
        UIUtility.printTableHeader3("Recommendation", "Author", "Comments");
        for (String[] d : discussions) {
            UIUtility.printTableRow3(
                    d.length > 0 ? d[0] : "",
                    d.length > 1 ? d[1] : "",
                    d.length > 2 ? d[2] : "0");
        }
        UIUtility.printSingleLine();
    }

    // ------------------------------------------------------------------ //
    //  Feedback
    // ------------------------------------------------------------------ //

    /**
     * Shows a confirmation that a new discussion was posted.
     *
     * @param title the title of the created discussion
     */
    public void showDiscussionCreated(String title) {
        UIUtility.printSuccess("Discussion '" + title + "' created successfully.");
    }

    /**
     * Shows a confirmation that a comment was posted.
     */
    public void showCommentPosted() {
        UIUtility.printSuccess("Comment posted successfully.");
    }

    /**
     * Displays a generic error message within the Discussions module.
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
