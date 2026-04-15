package com.shg.state;

import com.shg.model.Transaction;
import com.shg.model.SHGMember;
import com.shg.security.MemberRole;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Pending State Handler
 * Transaction is created but awaiting approval
 * Only ACCOUNTANT and PRESIDENT can approve
 */
@Component
public class PendingTransactionState implements TransactionStateHandler {

    @Override
    public void handle(Transaction transaction, SHGMember handler) {
        transaction.setState("PENDING");
        transaction.setUpdatedAt(LocalDateTime.now());
        
        System.out.println("Transaction #" + transaction.getId() + " is in PENDING state");
        System.out.println("  → Awaiting approval from: " + handler.getFullName() + " (" + handler.getRole() + ")");
        System.out.println("  → Amount: " + transaction.getAmount() + " " + transaction.getType());
    }

    @Override
    public boolean canTransitionTo(Transaction transaction, SHGMember transitioner) {
        // Any user can create a transaction (it starts in PENDING state)
        return true;
    }

    @Override
    public String getState() {
        return "PENDING";
    }

    @Override
    public int getRequiredRoleLevel() {
        return MemberRole.MEMBER.getLevel(); // All can create transactions initially
    }

    /**
     * Send approval notifications
     */
    public void sendApprovalNotification(Transaction transaction) {
        System.out.println("  → Approval notification sent to ACCOUNTANT and PRESIDENT");
        System.out.println("  → Please review transaction for approval or rejection");
    }
}
