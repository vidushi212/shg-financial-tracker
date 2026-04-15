package com.shg.observer;

import com.shg.model.SHGGroup;
import com.shg.model.SHGMember;

/**
 * Observer interface for balance change notifications
 * Observers with different roles get notified based on their permissions
 */
public interface BalanceObserver {
    
    /**
     * Called when group balance changes
     */
    void onBalanceChanged(SHGGroup group, double oldBalance, double newBalance, SHGMember changedBy);
    
    /**
     * Called when member balance changes
     */
    void onMemberBalanceChanged(SHGMember member, double savingsChange, double loanChange, SHGMember changedBy);

    /**
     * Get the role level this observer requires to be notified
     */
    int getRequiredRoleLevel();

    /**
     * Called when transaction is pending approval
     */
    void onTransactionPending(Long transactionId, double amount, SHGMember member);
}
