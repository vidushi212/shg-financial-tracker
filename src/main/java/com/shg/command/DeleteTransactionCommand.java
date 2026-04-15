package com.shg.command;

import com.shg.model.SHGMember;
import com.shg.model.Transaction;
import com.shg.security.RoleAuthorizationService;
import com.shg.service.TransactionService;

/**
 * Command to delete a transaction
 * Only ACCOUNTANT, TREASURER, and PRESIDENT can delete
 */
public class DeleteTransactionCommand implements TransactionCommand {

    private final TransactionService transactionService;
    private final RoleAuthorizationService authorizationService;
    private final Long transactionId;
    private final SHGMember executedBy;
    private Transaction deletedTransaction;

    public DeleteTransactionCommand(TransactionService transactionService,
                                   RoleAuthorizationService authorizationService,
                                   Long transactionId,
                                   SHGMember executedBy) {
        this.transactionService = transactionService;
        this.authorizationService = authorizationService;
        this.transactionId = transactionId;
        this.executedBy = executedBy;
    }

    @Override
    public void execute() {
        if (!hasPermission(executedBy)) {
            throw new SecurityException("User " + executedBy.getFullName() + " (" + executedBy.getRole() + 
                                       ") does not have permission to delete transactions");
        }

        // Store the transaction before deletion for undo
        deletedTransaction = transactionService.getTransactionById(transactionId)
                           .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        // Only allow deletion before approval
        if ("APPROVED".equals(deletedTransaction.getState())) {
            throw new IllegalStateException("Cannot delete approved transactions");
        }

        transactionService.deleteTransaction(transactionId);
        System.out.println("Transaction ID " + transactionId + " deleted by " + executedBy.getFullName());
    }

    @Override
    public void undo() {
        if (deletedTransaction != null) {
            // Recreate the deleted transaction (simplified - would need to handle state properly)
            transactionService.createTransaction(deletedTransaction);
            System.out.println("Transaction ID " + transactionId + " has been restored");
        }
    }

    @Override
    public String getDescription() {
        return "Delete transaction #" + transactionId + " by " + executedBy.getFullName();
    }

    @Override
    public boolean hasPermission(SHGMember member) {
        return authorizationService.canDeleteTransaction(member);
    }

    @Override
    public SHGMember getExecutedBy() {
        return executedBy;
    }
}
