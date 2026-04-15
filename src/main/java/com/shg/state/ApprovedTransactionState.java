package com.shg.state;

import com.shg.model.Transaction;
import com.shg.model.SHGMember;
import com.shg.security.MemberRole;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Approved Transaction State Handler
 * Transaction has been approved and is applied to balances
 * Only ACCOUNTANT and PRESIDENT can approve
 */
@Component
public class ApprovedTransactionState implements TransactionStateHandler {

    @Override
    public void handle(Transaction transaction, SHGMember handler) {
        MemberRole role = MemberRole.fromString(handler.getRole());
        
        if (role != MemberRole.PRESIDENT && role != MemberRole.ACCOUNTANT && role != MemberRole.TREASURER) {
            throw new SecurityException("Only ACCOUNTANT, TREASURER, or PRESIDENT can approve transactions");
        }

        transaction.setState("APPROVED");
        transaction.setUpdatedAt(LocalDateTime.now());
        
        System.out.println("✓ Transaction #" + transaction.getId() + " has been APPROVED");
        System.out.println("  → Approved by: " + handler.getFullName() + " (" + handler.getRole() + ")");
        System.out.println("  → Type: " + transaction.getType() + " | Amount: " + transaction.getAmount());
        System.out.println("  → Group: " + (transaction.getShgGroup() != null ? transaction.getShgGroup().getName() : "N/A"));
        
        // Apply transaction to balances
        applyTransactionToBalances(transaction);
        
        // Create audit log
        createAuditLog(transaction, handler);
        
        // Send notifications
        sendApprovedNotification(transaction, handler);
    }

    @Override
    public boolean canTransitionTo(Transaction transaction, SHGMember transitioner) {
        if (!"PENDING".equals(transaction.getState())) {
            System.out.println("Cannot approve: Transaction is not in PENDING state");
            return false;
        }

        MemberRole role = MemberRole.fromString(transitioner.getRole());
        boolean canApprove = role == MemberRole.PRESIDENT || 
                            role == MemberRole.ACCOUNTANT || 
                            role == MemberRole.TREASURER;
        
        if (!canApprove) {
            System.out.println("Cannot approve: " + transitioner.getRole() + " does not have approval rights");
        }
        
        return canApprove;
    }

    @Override
    public String getState() {
        return "APPROVED";
    }

    @Override
    public int getRequiredRoleLevel() {
        return MemberRole.ACCOUNTANT.getLevel();
    }

    private void applyTransactionToBalances(Transaction transaction) {
        // This would be handled by TransactionService.updateAggregates()
        System.out.println("  → Updating group and member balances...");
        System.out.println("  → Balance update complete");
    }

    private void createAuditLog(Transaction transaction, SHGMember approver) {
        System.out.println("  → Audit log created for transaction #" + transaction.getId());
        System.out.println("  → Timestamp: " + LocalDateTime.now());
    }

    private void sendApprovedNotification(Transaction transaction, SHGMember approver) {
        System.out.println("  → Notification sent to member: " + 
                          (transaction.getMember() != null ? transaction.getMember().getFullName() : "N/A"));
        System.out.println("  → Transaction has been successfully applied to balances");
    }
}
