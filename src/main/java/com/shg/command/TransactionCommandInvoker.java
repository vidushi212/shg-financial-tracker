package com.shg.command;

import com.shg.model.SHGMember;
import com.shg.security.MemberRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Command Invoker - Manages command execution, undo/redo, and history
 * Role-based tracking of who executed what and when
 */
@Service
public class TransactionCommandInvoker {

    private final Stack<TransactionCommand> commandHistory = new Stack<>();
    private final Stack<TransactionCommand> redoStack = new Stack<>();
    private final Map<TransactionCommand, CommandMetadata> commandMetadata = new HashMap<>();

    /**
     * Execute a command with role validation
     */
    public void executeCommand(TransactionCommand command) {
        SHGMember executor = command.getExecutedBy();
        
        if (!command.hasPermission(executor)) {
            throw new SecurityException("User " + executor.getFullName() + " (" + executor.getRole() + 
                                       ") does not have permission to execute: " + command.getDescription());
        }

        try {
            command.execute();
            commandHistory.push(command);
            redoStack.clear(); // Clear redo stack when new command is executed
            
            // Store metadata
            CommandMetadata metadata = new CommandMetadata(executor, LocalDateTime.now());
            commandMetadata.put(command, metadata);
            
            System.out.println("✓ Command executed: " + command.getDescription());
        } catch (Exception e) {
            System.err.println("✗ Command failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Undo last command - only ACCOUNTANT, TREASURER, PRESIDENT can undo others' commands
     */
    public void undoLastCommand(SHGMember requester) {
        if (commandHistory.isEmpty()) {
            throw new IllegalStateException("No commands to undo");
        }

        TransactionCommand command = commandHistory.pop();
        SHGMember originalExecutor = command.getExecutedBy();

        // Check if requester can undo this command
        if (!canUndoCommand(requester, originalExecutor)) {
            // Push it back if not allowed
            commandHistory.push(command);
            throw new SecurityException("User " + requester.getFullName() + " (" + requester.getRole() + 
                                       ") cannot undo command executed by " + originalExecutor.getFullName());
        }

        try {
            command.undo();
            redoStack.push(command);
            System.out.println("✓ Command undone: " + command.getDescription());
        } catch (Exception e) {
            // Push back if undo fails
            commandHistory.push(command);
            System.err.println("✗ Undo failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Redo last undone command
     */
    public void redoLastCommand(SHGMember requester) {
        if (redoStack.isEmpty()) {
            throw new IllegalStateException("No commands to redo");
        }

        TransactionCommand command = redoStack.pop();
        
        // Command should be re-execute, need to check permission again
        if (!command.hasPermission(requester)) {
            redoStack.push(command);
            throw new SecurityException("You no longer have permission to redo: " + command.getDescription());
        }

        try {
            command.execute();
            commandHistory.push(command);
            System.out.println("✓ Command redone: " + command.getDescription());
        } catch (Exception e) {
            redoStack.push(command);
            System.err.println("✗ Redo failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get command history - role-based filtering
     */
    public List<String> getCommandHistory(SHGMember requester) {
        MemberRole role = MemberRole.fromString(requester.getRole());
        
        return commandHistory.stream()
                .map(cmd -> formatCommandHistory(cmd, role, requester))
                .collect(Collectors.toList());
    }

    /**
     * Get full audit trail - only PRESIDENT can see everything
     */
    public List<String> getFullAuditTrail(SHGMember requester) {
        if (!MemberRole.fromString(requester.getRole()).equals(MemberRole.PRESIDENT)) {
            throw new SecurityException("Only PRESIDENT can view full audit trail");
        }

        return commandHistory.stream()
                .map(cmd -> {
                    CommandMetadata metadata = commandMetadata.get(cmd);
                    return String.format("(%s) %s by %s at %s",
                            cmd.getExecutedBy().getRole(),
                            cmd.getDescription(),
                            cmd.getExecutedBy().getFullName(),
                            metadata != null ? metadata.getExecutedAt() : "N/A");
                })
                .collect(Collectors.toList());
    }

    /**
     * Clear history - only PRESIDENT can do this
     */
    public void clearHistory(SHGMember requester) {
        if (!MemberRole.fromString(requester.getRole()).equals(MemberRole.PRESIDENT)) {
            throw new SecurityException("Only PRESIDENT can clear command history");
        }
        commandHistory.clear();
        redoStack.clear();
        commandMetadata.clear();
        System.out.println("Command history cleared by " + requester.getFullName());
    }

    /**
     * Check if requester can undo a command
     */
    private boolean canUndoCommand(SHGMember requester, SHGMember originalExecutor) {
        if (requester.getId().equals(originalExecutor.getId())) {
            return true; // Can undo own commands
        }

        MemberRole requesterRole = MemberRole.fromString(requester.getRole());
        
        // Only ACCOUNTANT, TREASURER, PRESIDENT can undo others' commands
        return requesterRole == MemberRole.ACCOUNTANT || 
               requesterRole == MemberRole.TREASURER ||
               requesterRole == MemberRole.PRESIDENT;
    }

    private String formatCommandHistory(TransactionCommand cmd, MemberRole role, SHGMember requester) {
        CommandMetadata metadata = commandMetadata.get(cmd);
        SHGMember executor = cmd.getExecutedBy();

        if (role == MemberRole.PRESIDENT) {
            // President sees everything
            return String.format("[%s] %s (by %s - %s)",
                    metadata != null ? metadata.getExecutedAt() : "N/A",
                    cmd.getDescription(),
                    executor.getFullName(),
                    executor.getRole());
        } else if (requester.getId().equals(executor.getId())) {
            // Others see only their own commands
            return String.format("[%s] %s",
                    metadata != null ? metadata.getExecutedAt() : "N/A",
                    cmd.getDescription());
        }
        
        return null; // Hide other users' commands from non-President
    }

    /**
     * Inner class for storing command metadata
     */
    private static class CommandMetadata {
        private final SHGMember executedBy;
        private final LocalDateTime executedAt;

        CommandMetadata(SHGMember executedBy, LocalDateTime executedAt) {
            this.executedBy = executedBy;
            this.executedAt = executedAt;
        }

        public SHGMember getExecutedBy() {
            return executedBy;
        }

        public LocalDateTime getExecutedAt() {
            return executedAt;
        }
    }
}
