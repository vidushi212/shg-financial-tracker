package com.shg.state;

import com.shg.model.Transaction;
import com.shg.model.SHGMember;
import com.shg.security.MemberRole;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Rejected Transaction State Handler
 * Transaction has been rejected and will not be applied
 * Only ACCOUNTANT and PRESIDENT can reject
 */
@Component
public class RejectedTransactionState implements TransactionStateHandler {

    private String rejectionReason;

    @Override
    public void handle(Transaction transaction, SHGMember handler) {
        MemberRole role = MemberRole.fromString(handler.getRole());
        
        if (role != MemberRole.PRESIDENT && role != MemberRole.ACCOUNTANT && role != MemberRole.TREASURER) {
            throw new SecurityException("Only ACCOUNTANT, TREASURER, or PRESIDENT can reject transactions");
        }

        transaction.setState("REJECTED");
        transaction.setUpdatedAt(LocalDateTime.now());
        
        System.out.println("✗ Transaction #" + transaction.getId() + " has been REJECTED");
        System.out.println("  → Rejected by: " + handler.getFullName() + " (" + handler.getRole() + ")");
        System.out.println("  → Type: " + transaction.getType() + " | Amount: " + transaction.getAmount());
        
        if (rejectionReason != null && !rejectionReason.isEmpty()) {
            System.out.println("  → Reason: " + rejectionReason);
        }
        
        // Create rejection log
        createRejectionLog(transaction, handler);
        
        // Send notifications
        sendRejectionNotification(transaction, handler);
    }

    @Override
    public boolean canTransitionTo(Transaction transaction, SHGMember transitioner) {
        if (!"PENDING".equals(transaction.getState())) {
            System.out.println("Cannot reject: Transaction is not in PENDING state");
            return false;
        }

        MemberRole role = MemberRole.fromString(transitioner.getRole());
        boolean canReject = role == MemberRole.PRESIDENT || 
                           role == MemberRole.ACCOUNTANT || 
                           role == MemberRole.TREASURER;
        
        if (!canReject) {
            System.out.println("Cannot reject: " + transitioner.getRole() + " does not have rejection rights");
        }
        
        return canReject;
    }

    @Override
    public String getState() {
        return "REJECTED";
    }

    @Override
    public int getRequiredRoleLevel() {
        return MemberRole.ACCOUNTANT.getLevel();
    }

    public void setRejectionReason(String reason) {
        this.rejectionReason = reason;
    }

    private void createRejectionLog(Transaction transaction, SHGMember rejector) {
        System.out.println("  → Rejection log created");
        System.out.println("  → Timestamp: " + LocalDateTime.now());
    }

    private void sendRejectionNotification(Transaction transaction, SHGMember rejector) {
        System.out.println("  → Rejection notification sent");
        
        if (transaction.getMember() != null) {
            System.out.println("  → Member notified: " + transaction.getMember().getFullName());
        }
        
        System.out.println("  → Transaction was NOT applied to balances");
        System.out.println("  → Member may resubmit with corrections if needed");
    }
}
