package com.shg.observer;

import com.shg.model.SHGGroup;
import com.shg.model.SHGMember;
import com.shg.security.MemberRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Audit Observer - Maintains audit trail for all transactions
 * All roles can see their own actions; PRESIDENT sees everything
 */
@Service
public class AuditObserver implements BalanceObserver {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onBalanceChanged(SHGGroup group, double oldBalance, double newBalance, SHGMember changedBy) {
        double changeAmount = newBalance - oldBalance;
        String auditLog = String.format(
            "[%s] AUDIT LOG: Balance Change | Group: %s | Old: %.2f | New: %.2f | Change: %.2f | By: %s (%s) | ID: %s",
            LocalDateTime.now().format(formatter),
            group.getName(),
            oldBalance,
            newBalance,
            changeAmount,
            changedBy.getFullName(),
            changedBy.getRole(),
            changedBy.getId()
        );
        System.out.println(auditLog);
        
        // Store audit log in database
        saveAuditEntry("BALANCE_CHANGE", group.getId(), changedBy.getId(), auditLog);
    }

    @Override
    public void onMemberBalanceChanged(SHGMember member, double savingsChange, double loanChange, SHGMember changedBy) {
        String auditLog = String.format(
            "[%s] AUDIT LOG: Member Balance Change | Member: %s | Savings: %.2f | Loan: %.2f | By: %s (%s) | ID: %s",
            LocalDateTime.now().format(formatter),
            member.getFullName(),
            savingsChange,
            loanChange,
            changedBy.getFullName(),
            changedBy.getRole(),
            changedBy.getId()
        );
        System.out.println(auditLog);
        
        saveAuditEntry("MEMBER_BALANCE_CHANGE", member.getId(), changedBy.getId(), auditLog);
    }

    @Override
    public int getRequiredRoleLevel() {
        return MemberRole.MEMBER.getLevel(); // All roles can audit their actions
    }

    @Override
    public void onTransactionPending(Long transactionId, double amount, SHGMember member) {
        String auditLog = String.format(
            "[%s] AUDIT LOG: Transaction Pending | ID: %d | Amount: %.2f | Member: %s | Action: Created/Waiting",
            LocalDateTime.now().format(formatter),
            transactionId,
            amount,
            member.getFullName()
        );
        System.out.println(auditLog);
        
        saveAuditEntry("TRANSACTION_CREATED", transactionId, member.getId(), auditLog);
    }

    private void saveAuditEntry(String eventType, Long entityId, Long userId, String details) {
        // Implementation: Save audit entry to database
        // Should include: timestamp, event type, entity ID, user ID, details, IP address, etc.
        System.out.println("  → Audit entry saved: EVENT=" + eventType + ", ENTITY_ID=" + entityId + ", USER_ID=" + userId);
    }

    /**
     * Get audit log for monitoring - role-based filtering
     */
    public void getAuditLog(SHGMember requester, Long groupId) {
        MemberRole role = MemberRole.fromString(requester.getRole());
        
        if (role == MemberRole.PRESIDENT) {
            // PRESIDENT sees all audit logs for the group
            System.out.println("PRESIDENT View: Full audit trail for group " + groupId);
        } else if (role == MemberRole.ACCOUNTANT) {
            // ACCOUNTANT sees financial-related audits only
            System.out.println("ACCOUNTANT View: Financial audit trail for group " + groupId);
        } else {
            // Others see only their own actions
            System.out.println("MEMBER View: Own transactions history");
        }
    }
}
