package com.shg.state;

import com.shg.model.Transaction;
import com.shg.model.SHGMember;
import com.shg.security.RoleAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Transaction State Manager
 * Manages state transitions and applies state handlers
 * Role-based state transition control
 */
@Service
public class TransactionStateManager {

    @Autowired
    private PendingTransactionState pendingState;

    @Autowired
    private ApprovedTransactionState approvedState;

    @Autowired
    private RejectedTransactionState rejectedState;

    @Autowired
    private RoleAuthorizationService authorizationService;

    private final Map<String, TransactionStateHandler> stateHandlers = new HashMap<>();

    public TransactionStateManager() {
    }

    @Autowired
    public void registerStateHandlers(PendingTransactionState pending,
                                     ApprovedTransactionState approved,
                                     RejectedTransactionState rejected) {
        stateHandlers.put("PENDING", pending);
        stateHandlers.put("APPROVED", approved);
        stateHandlers.put("REJECTED", rejected);
    }

    /**
     * Process transaction in its current state
     */
    public void processTransaction(Transaction transaction, SHGMember handler) {
        String currentState = transaction.getState();
        if (currentState == null) {
            currentState = "PENDING";
            transaction.setState("PENDING");
        }

        TransactionStateHandler stateHandler = stateHandlers.get(currentState);
        if (stateHandler != null) {
            stateHandler.handle(transaction, handler);
        }
    }

    /**
     * Approve a transaction - transition from PENDING to APPROVED
     */
    public void approveTransaction(Transaction transaction, SHGMember approver) {
        if (!authorizationService.canApproveTransactions(approver)) {
            throw new SecurityException("User " + approver.getFullName() + " (" + approver.getRole() + 
                                       ") does not have permission to approve transactions");
        }

        if (!approvedState.canTransitionTo(transaction, approver)) {
            throw new IllegalStateException("Cannot approve transaction in state: " + transaction.getState());
        }

        System.out.println("\n=== APPROVING TRANSACTION ===");
        approvedState.handle(transaction, approver);
        System.out.println("=============================\n");
    }

    /**
     * Reject a transaction - transition from PENDING to REJECTED
     */
    public void rejectTransaction(Transaction transaction, SHGMember rejector, String reason) {
        if (!authorizationService.canApproveTransactions(rejector)) {
            throw new SecurityException("User " + rejector.getFullName() + " (" + rejector.getRole() + 
                                       ") does not have permission to reject transactions");
        }

        if (!rejectedState.canTransitionTo(transaction, rejector)) {
            throw new IllegalStateException("Cannot reject transaction in state: " + transaction.getState());
        }

        System.out.println("\n=== REJECTING TRANSACTION ===");
        rejectedState.setRejectionReason(reason);
        rejectedState.handle(transaction, rejector);
        System.out.println("=============================\n");
    }

    /**
     * Get current state handler
     */
    public TransactionStateHandler getStateHandler(String state) {
        return stateHandlers.get(state);
    }

    /**
     * Check if a member can transition to a specific state
     */
    public boolean canTransitionTo(Transaction transaction, String newState, SHGMember member) {
        TransactionStateHandler handler = stateHandlers.get(newState);
        if (handler == null) {
            return false;
        }
        return handler.canTransitionTo(transaction, member);
    }

    /**
     * Get all available state transitions for a user
     */
    public String getAvailableTransitions(Transaction transaction, SHGMember member) {
        StringBuilder sb = new StringBuilder();
        sb.append("Available transitions for ").append(member.getFullName()).append(" (").append(member.getRole()).append("):\n");

        if ("PENDING".equals(transaction.getState())) {
            sb.append("  → APPROVED (if authorized)\n");
            sb.append("  → REJECTED (if authorized)\n");
        } else {
            sb.append("  None - Transaction is in final state\n");
        }

        return sb.toString();
    }
}
