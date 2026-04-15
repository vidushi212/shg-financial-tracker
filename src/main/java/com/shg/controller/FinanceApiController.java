package com.shg.controller;

import com.shg.facade.FinanceFacade;
import com.shg.model.SHGMember;
import com.shg.model.Transaction;
import com.shg.security.RoleAuthorizationService;
import com.shg.service.AccountantService;
import com.shg.service.TransactionService;
import com.shg.state.TransactionStateManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FinanceApiController {

    private final FinanceFacade financeFacade;
    private final AccountantService accountantService;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private RoleAuthorizationService roleAuthorizationService;
    
    @Autowired(required = false)
    private TransactionStateManager stateManager;

    public FinanceApiController(FinanceFacade financeFacade,
                                AccountantService accountantService) {
        this.financeFacade = financeFacade;
        this.accountantService = accountantService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Map<String, Object>>> getTransactions() {
        return ResponseEntity.ok(financeFacade.getTransactions());
    }

    @PostMapping("/transactions")
    public ResponseEntity<Map<String, Object>> createTransaction(@RequestBody Map<String, String> payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeFacade.createTransaction(payload));
    }

    @GetMapping("/reports/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyReports() {
        return ResponseEntity.ok(financeFacade.getMonthlyReports());
    }

    @GetMapping("/accountant/overview")
    public ResponseEntity<Map<String, Object>> getAccountantOverview() {
        return ResponseEntity.ok(accountantService.getOverview());
    }

    @GetMapping("/accountant/member-accounts")
    public ResponseEntity<List<Map<String, Object>>> getMemberAccounts() {
        return ResponseEntity.ok(accountantService.getMemberAccountSummaries());
    }

    @GetMapping("/accountant/reports")
    public ResponseEntity<Map<String, Object>> getAccountantReports() {
        return ResponseEntity.ok(accountantService.getReportingSnapshot());
    }

    @PostMapping("/accountant/loans")
    public ResponseEntity<Map<String, Object>> disburseLoan(@RequestBody Map<String, String> payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountantService.disburseLoan(payload));
    }

    @PostMapping("/accountant/repayments")
    public ResponseEntity<Map<String, Object>> recordRepayment(@RequestBody Map<String, String> payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountantService.recordRepayment(payload));
    }

    // ============ Role-Based Transaction Management Endpoints ============

    /**
     * Approve a transaction - Only ACCOUNTANT, TREASURER, PRESIDENT
     */
    @PostMapping("/transactions/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveTransaction(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        
        try {
            // This should be replaced with actual authenticated user from SecurityContext
            // For now, we'll use a mock user from the request
            SHGMember approver = createMockUser(payload.get("approverUserId"), payload.get("approverRole"));
            
            // Check authorization
            if (!roleAuthorizationService.canApproveTransactions(approver)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "User does not have permission to approve transactions"));
            }

            Optional<Transaction> transaction = transactionService.getTransactionById(id);
            if (!transaction.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            // Approve the transaction
            transactionService.approveTransaction(id, approver);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Transaction approved successfully");
            response.put("transactionId", id);
            response.put("approvedBy", approver.getFullName());
            response.put("transactionState", "APPROVED");
            
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Reject a transaction - Only ACCOUNTANT, TREASURER, PRESIDENT
     */
    @PostMapping("/transactions/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectTransaction(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        
        try {
            // This should be replaced with actual authenticated user
            SHGMember rejector = createMockUser(payload.get("rejectorUserId"), payload.get("rejectorRole"));
            String reason = (String) payload.getOrDefault("reason", "No reason provided");
            
            // Check authorization
            if (!roleAuthorizationService.canApproveTransactions(rejector)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "User does not have permission to reject transactions"));
            }

            Optional<Transaction> transaction = transactionService.getTransactionById(id);
            if (!transaction.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            // Reject the transaction
            transactionService.rejectTransaction(id, rejector, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Transaction rejected successfully");
            response.put("transactionId", id);
            response.put("rejectedBy", rejector.getFullName());
            response.put("rejectionReason", reason);
            response.put("transactionState", "REJECTED");
            
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get transaction state - shows current state and available actions based on role
     */
    @GetMapping("/transactions/{id}/state")
    public ResponseEntity<Map<String, Object>> getTransactionState(
            @PathVariable Long id,
            @RequestParam String userRole) {
        
        Optional<Transaction> transaction = transactionService.getTransactionById(id);
        if (!transaction.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", id);
        response.put("currentState", transaction.get().getState());
        
        // Add available actions based on role
        if ("PRESIDENT".equals(userRole) || "ACCOUNTANT".equals(userRole) || "TREASURER".equals(userRole)) {
            if ("PENDING".equals(transaction.get().getState())) {
                response.put("availableActions", List.of("APPROVE", "REJECT"));
            } else {
                response.put("availableActions", List.of());
            }
        } else {
            response.put("availableActions", List.of());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all pending transactions for approval - role-based filtering
     */
    @GetMapping("/transactions/pending")
    public ResponseEntity<Map<String, Object>> getPendingTransactions(@RequestParam String userRole) {
        List<Transaction> allTransactions = transactionService.getAllTransactions();
        
        // Filter based on role
        List<Transaction> pendingTransactions = allTransactions.stream()
                .filter(t -> "PENDING".equals(t.getState()))
                .toList();
        
        Map<String, Object> response = new HashMap<>();
        response.put("role", userRole);
        response.put("canApprove", "PRESIDENT".equals(userRole) || "ACCOUNTANT".equals(userRole) || "TREASURER".equals(userRole));
        response.put("pendingCount", pendingTransactions.size());
        response.put("transactions", pendingTransactions);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Mock user creation - Replace with actual authentication in production
     */
    private SHGMember createMockUser(Object userId, Object role) {
        SHGMember user = new SHGMember();
        user.setId(userId instanceof Number ? ((Number) userId).longValue() : 1L);
        user.setFullName("User " + userId);
        user.setRole(role != null ? role.toString() : "MEMBER");
        user.setUsername("user_" + userId);
        user.setPassword("mock_password");
        return user;
    }
}
