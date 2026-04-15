package com.shg.command;

import com.shg.model.SHGMember;

/**
 * Command interface for transaction operations
 * Supports role-based authorization and undo/redo
 */
public interface TransactionCommand {
    
    /**
     * Execute the command
     */
    void execute();
    
    /**
     * Undo the command
     */
    void undo();
    
    /**
     * Get command description for logging
     */
    String getDescription();

    /**
     * Check if the executing member has permission to run this command
     */
    boolean hasPermission(SHGMember member);

    /**
     * Get the member who executed this command
     */
    SHGMember getExecutedBy();
}
