package com.shg.command;

import com.shg.model.SHGMember;
import com.shg.model.Transaction;
import com.shg.security.RoleAuthorizationService;
import com.shg.service.TransactionService;

/**
 * Command to create a transaction
 * Only ACCOUNTANT, TREASURER, SECRETARY, and PRESIDENT can create
 */
public class CreateTransactionCommand implements TransactionCommand {

    private final TransactionService transactionService;
    private final RoleAuthorizationService authorizationService;
    private final Transaction transaction;
    private final SHGMember executedBy;
    private Transaction createdTransaction;

    public CreateTransactionCommand(TransactionService transactionService,
                                   RoleAuthorizationService authorizationService,
                                   Transaction transaction,
                                   SHGMember executedBy) {
        this.transactionService = transactionService;
        this.authorizationService = authorizationService;
        this.transaction = transaction;
        this.executedBy = executedBy;
    }

    @Override
    public void execute() {
        if (!hasPermission(executedBy)) {
            throw new SecurityException("User " + executedBy.getFullName() + " (" + executedBy.getRole() + 
                                       ") does not have permission to create transactions");
        }

        // Set the person who created this transaction
        transaction.setRecordedBy(executedBy.getUsername());
        
        // Create transaction with initial state as PENDING
        createdTransaction = transactionService.createTransaction(transaction);
        
        System.out.println("Transaction created by " + executedBy.getFullName() + " (ID: " + createdTransaction.getId() + 
                          ") - Amount: " + createdTransaction.getAmount() + " - Amount: " + createdTransaction.getAmount());
    }

    @Override
    public void undo() {
        if (createdTransaction != null) {
            transactionService.deleteTransaction(createdTransaction.getId());
            System.out.println("Transaction ID " + createdTransaction.getId() + " has been undone");
        }
    }

    @Override
    public String getDescription() {
        return "Create transaction: " + transaction.getType() + " - Amount: " + transaction.getAmount() + 
               " by " + executedBy.getFullName();
    }

    @Override
    public boolean hasPermission(SHGMember member) {
        return authorizationService.canCreateTransaction(member);
    }

    @Override
    public SHGMember getExecutedBy() {
        return executedBy;
    }

    public Transaction getCreatedTransaction() {
        return createdTransaction;
    }
}
