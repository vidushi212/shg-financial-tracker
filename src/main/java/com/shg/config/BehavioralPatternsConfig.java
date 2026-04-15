package com.shg.config;

import com.shg.observer.*;
import com.shg.security.RoleAuthorizationService;
import com.shg.service.TransactionService;
import com.shg.state.ApprovedTransactionState;
import com.shg.state.PendingTransactionState;
import com.shg.state.RejectedTransactionState;
import com.shg.state.TransactionStateHandler;
import com.shg.strategy.CompoundInterestStrategy;
import com.shg.strategy.InterestCalculationStrategy;
import com.shg.strategy.SimpleInterestStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for Behavioral Patterns
 * Wires all observers, strategies, and state handlers with dependencies
 */
@Configuration
public class BehavioralPatternsConfig {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private RoleAuthorizationService authorizationService;

    /**
     * Wire observers to TransactionService
     * Ensures role-based notifications are set up
     */
    @Autowired
    public void configureObservers(TransactionService transactionService,
                                  DashboardObserver dashboardObserver,
                                  ReportObserver reportObserver,
                                  AuditObserver auditObserver) {
        
        System.out.println("\n=== Configuring Observers ===");
        
        // Cast to BalanceSubject if TransactionService implements it
        if (transactionService instanceof BalanceSubject) {
            BalanceSubject subject = (BalanceSubject) transactionService;
            subject.addObserver(dashboardObserver);
            subject.addObserver(reportObserver);
            subject.addObserver(auditObserver);
            
            System.out.println("✓ DashboardObserver registered (Level: " + dashboardObserver.getRequiredRoleLevel() + ")");
            System.out.println("✓ ReportObserver registered (Level: " + reportObserver.getRequiredRoleLevel() + ")");
            System.out.println("✓ AuditObserver registered (Level: " + auditObserver.getRequiredRoleLevel() + ")");
        }
        
        System.out.println("=============================\n");
    }

    /**
     * Provide strategies for interest calculation
     */
    @Bean
    public Map<String, InterestCalculationStrategy> provideStrategies() {
        System.out.println("\n=== Configuring Strategies ===");
        
        Map<String, InterestCalculationStrategy> strategies = new HashMap<>();
        strategies.put("simpleInterest", new SimpleInterestStrategy());
        strategies.put("compoundInterest", new CompoundInterestStrategy());
        
        System.out.println("✓ SimpleInterestStrategy registered");
        System.out.println("✓ CompoundInterestStrategy registered");
        System.out.println("===============================\n");
        
        return strategies;
    }

    /**
     * Provide state handlers for transaction processing
     */
    @Bean
    public Map<String, TransactionStateHandler> provideStateHandlers(
            PendingTransactionState pendingState,
            ApprovedTransactionState approvedState,
            RejectedTransactionState rejectedState) {
        
        System.out.println("\n=== Configuring State Handlers ===");
        
        Map<String, TransactionStateHandler> handlers = new HashMap<>();
        handlers.put("PENDING", pendingState);
        handlers.put("APPROVED", approvedState);
        handlers.put("REJECTED", rejectedState);
        
        System.out.println("✓ PendingTransactionState registered");
        System.out.println("✓ ApprovedTransactionState registered");
        System.out.println("✓ RejectedTransactionState registered");
        System.out.println("===================================\n");
        
        return handlers;
    }

    /**
     * Print initialization summary
     */
    @Bean
    public String printBehavioralPatternsInitSummary(
            RoleAuthorizationService authService) {
        
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  BEHAVIORAL PATTERNS INITIALIZED SUCCESSFULLY               ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║  OBSERVER PATTERN:                                         ║");
        System.out.println("║    • DashboardObserver  - Updates dashboards               ║");
        System.out.println("║    • ReportObserver     - Triggers report generation       ║");
        System.out.println("║    • AuditObserver      - Maintains audit trails           ║");
        System.out.println("║                                                            ║");
        System.out.println("║  COMMAND PATTERN:                                          ║");
        System.out.println("║    • CreateTransactionCommand   - Create transactions      ║");
        System.out.println("║    • DeleteTransactionCommand   - Delete transactions      ║");
        System.out.println("║    • CommandInvoker - Manages undo/redo with history       ║");
        System.out.println("║                                                            ║");
        System.out.println("║  STRATEGY PATTERN:                                         ║");
        System.out.println("║    • SimpleInterestStrategy      - Basic interest calc     ║");
        System.out.println("║    • CompoundInterestStrategy    - Compounded interest     ║");
        System.out.println("║                                                            ║");
        System.out.println("║  STATE PATTERN:                                            ║");
        System.out.println("║    • PendingState    - Awaiting approval                   ║");
        System.out.println("║    • ApprovedState   - Applied to balances                 ║");
        System.out.println("║    • RejectedState   - Rejected and logged                 ║");
        System.out.println("║                                                            ║");
        System.out.println("║  ROLE-BASED ACCESS CONTROL:                                ║");
        System.out.println("║    • PRESIDENT       - Full access, system monitoring      ║");
        System.out.println("║    • ACCOUNTANT      - Financial operations                ║");
        System.out.println("║    • TREASURER       - Financial oversight                 ║");
        System.out.println("║    • SECRETARY       - Member management                   ║");
        System.out.println("║    • MEMBER          - Limited access to own data          ║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        return "Behavioral Patterns Config Loaded";
    }
}
