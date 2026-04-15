package com.shg.observer;

import com.shg.model.SHGGroup;
import com.shg.model.SHGMember;
import com.shg.security.MemberRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Dashboard Observer - Updates dashboard when balances change
 * Notifies ACCOUNTANT, PRESIDENT, and higher roles
 */
@Service
public class DashboardObserver implements BalanceObserver {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onBalanceChanged(SHGGroup group, double oldBalance, double newBalance, SHGMember changedBy) {
        // Only notify if changed by authorized person
        MemberRole role = MemberRole.fromString(changedBy.getRole());
        if (role.getLevel() >= MemberRole.ACCOUNTANT.getLevel()) {
            String message = String.format(
                "[%s] Dashboard Update: Group '%s' balance changed from %.2f to %.2f by %s (%s)",
                LocalDateTime.now().format(formatter),
                group.getName(),
                oldBalance,
                newBalance,
                changedBy.getFullName(),
                changedBy.getRole()
            );
            System.out.println(message);
            
            // Here you would update dashboard cache, send WebSocket notifications, etc.
            updateDashboardCache(group, newBalance);
        }
    }

    @Override
    public void onMemberBalanceChanged(SHGMember member, double savingsChange, double loanChange, SHGMember changedBy) {
        MemberRole role = MemberRole.fromString(changedBy.getRole());
        if (role.getLevel() >= MemberRole.ACCOUNTANT.getLevel()) {
            String message = String.format(
                "[%s] Dashboard Update: Member '%s' - Savings change: %.2f, Loan change: %.2f (by %s)",
                LocalDateTime.now().format(formatter),
                member.getFullName(),
                savingsChange,
                loanChange,
                changedBy.getFullName()
            );
            System.out.println(message);
            updateMemberDashboard(member);
        }
    }

    @Override
    public int getRequiredRoleLevel() {
        return MemberRole.ACCOUNTANT.getLevel();
    }

    @Override
    public void onTransactionPending(Long transactionId, double amount, SHGMember member) {
        String message = String.format(
            "[%s] Dashboard Alert: Transaction #%d pending approval - Amount: %.2f by %s",
            LocalDateTime.now().format(formatter),
            transactionId,
            amount,
            member.getFullName()
        );
        System.out.println(message);
    }

    private void updateDashboardCache(SHGGroup group, double newBalance) {
        // Implementation: Update real-time dashboard cache
        // Could use Redis, WebSocket, or in-memory cache
    }

    private void updateMemberDashboard(SHGMember member) {
        // Implementation: Update member-specific dashboard
    }
}
