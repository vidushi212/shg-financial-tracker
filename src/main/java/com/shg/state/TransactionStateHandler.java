package com.shg.state;

import com.shg.model.Transaction;
import com.shg.model.SHGMember;

/**
 * Transaction State Handler interface
 * Defines behavior for different transaction states
 */
public interface TransactionStateHandler {
    
    /**
     * Handle transition to this state
     */
    void handle(Transaction transaction, SHGMember handler);
    
    /**
     * Check if transition to this state is allowed
     */
    boolean canTransitionTo(Transaction transaction, SHGMember transitioner);
    
    /**
     * Get the state this handler represents
     */
    String getState();

    /**
     * Get required role level for this state transition
     */
    int getRequiredRoleLevel();
}
