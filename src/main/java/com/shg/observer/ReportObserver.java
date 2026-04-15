package com.shg.observer;

import com.shg.model.SHGGroup;
import com.shg.model.SHGMember;
import com.shg.security.MemberRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Report Observer - Triggers report recalculation
 * Only ACCOUNTANT, TREASURER, and PRESIDENT can trigger report generation
 */
@Service
public class ReportObserver implements BalanceObserver {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onBalanceChanged(SHGGroup group, double oldBalance, double newBalance, SHGMember changedBy) {
        MemberRole role = MemberRole.fromString(changedBy.getRole());
        
        // Only financial roles can trigger reports
        if (role == MemberRole.ACCOUNTANT || role == MemberRole.TREASURER || role == MemberRole.PRESIDENT) {
            String message = String.format(
                "[%s] Report Triggered: Group '%s' balance changed - Recalculating reports (by %s)",
                LocalDateTime.now().format(formatter),
                group.getName(),
                changedBy.getFullName()
            );
            System.out.println(message);
            
            // Trigger async report generation
            triggerReportGeneration(group);
        }
    }

    @Override
    public void onMemberBalanceChanged(SHGMember member, double savingsChange, double loanChange, SHGMember changedBy) {
        MemberRole role = MemberRole.fromString(changedBy.getRole());
        
        if (role == MemberRole.ACCOUNTANT || role == MemberRole.TREASURER || role == MemberRole.PRESIDENT) {
            String message = String.format(
                "[%s] Report Update: Member '%s' contribution updated - Updating annual reports (by %s)",
                LocalDateTime.now().format(formatter),
                member.getFullName(),
                changedBy.getFullName()
            );
            System.out.println(message);
            
            triggerMemberReportUpdate(member);
        }
    }

    @Override
    public int getRequiredRoleLevel() {
        return MemberRole.ACCOUNTANT.getLevel();
    }

    @Override
    public void onTransactionPending(Long transactionId, double amount, SHGMember member) {
        String message = String.format(
            "[%s] Report Note: Transaction #%d (%s) requires verification in reports",
            LocalDateTime.now().format(formatter),
            transactionId,
            amount
        );
        System.out.println(message);
    }

    private void triggerReportGeneration(SHGGroup group) {
        // Implementation: Generate/update group financial reports
        // Could include: monthly reports, quarterly summaries, annual statements
        System.out.println("  → Monthly report generation triggered for group: " + group.getName());
    }

    private void triggerMemberReportUpdate(SHGMember member) {
        // Implementation: Update member-specific reports
        // Could include: contribution statements, savings history, loan records
        System.out.println("  → Member contribution report updated for: " + member.getFullName());
    }
}
