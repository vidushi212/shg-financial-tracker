package com.shg.observer;

/**
 * Subject interface for balance change notifications
 * Manages observers and notifies them of changes
 */
public interface BalanceSubject {
    
    /**
     * Add an observer
     */
    void addObserver(BalanceObserver observer);
    
    /**
     * Remove an observer
     */
    void removeObserver(BalanceObserver observer);
    
    /**
     * Notify observers about balance change
     */
    void notifyBalanceChange(Object group, double oldBalance, double newBalance, Object changedBy);
    
    /**
     * Notify observers about member balance change
     */
    void notifyMemberBalanceChange(Object member, double savingsChange, double loanChange, Object changedBy);

    /**
     * Notify about pending transaction requiring approval
     */
    void notifyTransactionPending(Long transactionId, double amount, Object member);
}
