# Detailed File Structure & Implementation Paths

## Complete File Locations & Details

### PATTERN 1: OBSERVER PATTERN FILES

#### Location: `src/main/java/com/shg/observer/`

```
observer/
├── BalanceObserver.java
│   ├── Type: Interface
│   ├── Visibility: public
│   ├── Methods:
│   │   - void onBalanceChanged(SHGGroup group, double oldBalance, double newBalance)
│   │   - void onMemberBalanceChanged(SHGMember member, double savingsChange, double loanChange)
│   └── Size: ~10 lines
│
├── BalanceSubject.java
│   ├── Type: Interface
│   ├── Visibility: public
│   ├── Methods:
│   │   - void addObserver(BalanceObserver observer)
│   │   - void removeObserver(BalanceObserver observer)
│   │   - void notifyBalanceChange(SHGGroup group, double oldBalance, double newBalance)
│   │   - void notifyMemberBalanceChange(SHGMember member, double savingsChange, double loanChange)
│   └── Size: ~15 lines
│
├── DashboardObserver.java
│   ├── Type: @Service (Spring Component)
│   ├── Implements: BalanceObserver
│   ├── Responsibilities:
│   │   - Update dashboard when group balance changes
│   │   - Update member dashboard data
│   │   - Send real-time notifications (WebSocket optional)
│   └── Size: ~30 lines
│
├── ReportObserver.java
│   ├── Type: @Service (Spring Component)
│   ├── Implements: BalanceObserver
│   ├── Responsibilities:
│   │   - Trigger report recalculation on balance changes
│   │   - Update cached reports
│   │   - Notify report generation services
│   └── Size: ~30 lines
│
└── AuditObserver.java
    ├── Type: @Service (Spring Component)
    ├── Implements: BalanceObserver
    ├── Responsibilities:
    │   - Log all balance changes
    │   - Maintain audit trail
    │   - Track who made changes and when
    └── Size: ~30 lines
```

**Modified File:**
- `src/main/java/com/shg/service/TransactionService.java`
  - Add `implements BalanceSubject`
  - Add `private List<BalanceObserver> observers = new ArrayList<>()`
  - Add observer management methods
  - Modify `updateAggregates()` to call notify methods

---

### PATTERN 2: COMMAND PATTERN FILES

#### Location: `src/main/java/com/shg/command/`

```
command/
├── TransactionCommand.java
│   ├── Type: Interface
│   ├── Visibility: public
│   ├── Methods:
│   │   - void execute()
│   │   - void undo()
│   │   - String getDescription()
│   └── Size: ~12 lines
│
├── CreateTransactionCommand.java
│   ├── Type: @Component (or leave as plain class)
│   ├── Implements: TransactionCommand
│   ├── Fields:
│   │   - TransactionService transactionService
│   │   - Transaction transaction
│   │   - Transaction createdTransaction (for undo)
│   ├── Methods:
│   │   - execute(): creates transaction via service
│   │   - undo(): deletes created transaction
│   │   - getDescription(): returns operation description
│   └── Size: ~40 lines
│
├── UpdateTransactionCommand.java
│   ├── Type: @Component
│   ├── Implements: TransactionCommand
│   ├── Fields:
│   │   - TransactionService transactionService
│   │   - Transaction transaction
│   │   - Transaction oldTransaction (for undo)
│   ├── Methods:
│   │   - execute(): updates transaction
│   │   - undo(): restores old values
│   │   - getDescription(): returns operation description
│   └── Size: ~40 lines
│
├── DeleteTransactionCommand.java
│   ├── Type: @Component
│   ├── Implements: TransactionCommand
│   ├── Fields:
│   │   - TransactionService transactionService
│   │   - Long transactionId
│   │   - Transaction deletedTransaction (for undo)
│   ├── Methods:
│   │   - execute(): deletes transaction
│   │   - undo(): restores deleted transaction
│   │   - getDescription(): returns operation description
│   └── Size: ~40 lines
│
└── TransactionCommandInvoker.java
    ├── Type: @Service (Spring Bean)
    ├── Fields:
    │   - Stack<TransactionCommand> commandHistory
    │   - Stack<TransactionCommand> redoStack
    ├── Methods:
    │   - void executeCommand(TransactionCommand command)
    │   - void undoLastCommand()
    │   - void redoLastCommand()
    │   - List<String> getCommandHistory()
    │   - List<String> getRedoHistory()
    │   - void clearHistory()
    └── Size: ~60 lines
```

**Modified Files:**
- `src/main/java/com/shg/controller/TransactionController.java`
  - Add `@Autowired TransactionCommandInvoker commandInvoker`
  - Replace direct service calls in `createTransaction()` with command
  - Replace service calls in `updateTransaction()` (if exists) with command
  - Replace service calls in `deleteTransaction()` (if exists) with command
  - Add `@PostMapping("/undo")` endpoint
  - Add `@PostMapping("/redo")` endpoint
  - Add `@GetMapping("/history")` endpoint

- `src/main/java/com/shg/service/TransactionService.java`
  - Add `deleteTransaction(Long id)` method (if not exists)
  - Add `updateTransaction(Transaction transaction)` method (if not exists)

---

### PATTERN 3: STRATEGY PATTERN FILES

#### Location: `src/main/java/com/shg/strategy/`

```
strategy/
├── InterestCalculationStrategy.java
│   ├── Type: Interface
│   ├── Visibility: public
│   ├── Methods:
│   │   - double calculateInterest(double principal, double rate, int timePeriod)
│   └── Size: ~8 lines
│
├── SimpleInterestStrategy.java
│   ├── Type: @Component("simpleInterest")
│   ├── Implements: InterestCalculationStrategy
│   ├── Formula: I = P × R × T
│   ├── Methods:
│   │   - double calculateInterest(double principal, double rate, int timePeriod)
│   └── Size: ~15 lines
│
├── CompoundInterestStrategy.java
│   ├── Type: @Component("compoundInterest")
│   ├── Implements: InterestCalculationStrategy
│   ├── Formula: A = P(1 + r)^t
│   ├── Methods:
│   │   - double calculateInterest(double principal, double rate, int timePeriod)
│   └── Size: ~15 lines
│
└── FinancialCalculationService.java
    ├── Type: @Service (Spring Bean)
    ├── Fields:
    │   - InterestCalculationStrategy strategy
    │   - Map<String, InterestCalculationStrategy> strategies (autowired)
    ├── Methods:
    │   - void setStrategy(String strategyName)
    │   - double calculateInterest(double principal, double rate, int timePeriod)
    │   - List<String> getAvailableStrategies()
    │   - double compareStrategies(double principal, double rate, int timePeriod)
    └── Size: ~50 lines
```

**Modified Files (Create or Modify):**
- `src/main/java/com/shg/service/AdvisoryService.java` (or create new)
  - Add `@Autowired FinancialCalculationService calculationService`
  - Add method to calculate projected savings using strategies
  - Add method to calculate investment returns
  - Add method to recommend schemes based on strategy

- `src/main/java/com/shg/controller/AdvisoryApiController.java` (or create endpoint)
  - Add `@PostMapping("/calculate-interest")` with strategy parameter
  - Add `@PostMapping("/project-savings")` endpoint
  - Add `@GetMapping("/calculation-methods")` to list available strategies

---

### PATTERN 4: STATE PATTERN FILES

#### Location: `src/main/java/com/shg/state/`

```
state/
├── TransactionState.java (or in model/)
│   ├── Type: Enum
│   ├── Values:
│   │   - PENDING
│   │   - APPROVED
│   │   - REJECTED
│   └── Size: ~8 lines
│
├── TransactionStateHandler.java
│   ├── Type: Interface
│   ├── Visibility: public
│   ├── Methods:
│   │   - void handle(Transaction transaction, TransactionService service)
│   │   - TransactionState getState()
│   └── Size: ~12 lines
│
├── PendingTransactionState.java
│   ├── Type: @Component
│   ├── Implements: TransactionStateHandler
│   ├── Responsibilities:
│   │   - Validate transaction data
│   │   - Send notification to approvers
│   │   - Prepare for approval workflow
│   ├── Methods:
│   │   - void handle(Transaction transaction, TransactionService service)
│   │   - TransactionState getState()
│   └── Size: ~35 lines
│
├── ApprovedTransactionState.java
│   ├── Type: @Component
│   ├── Implements: TransactionStateHandler
│   ├── Responsibilities:
│   │   - Apply transaction to balances
│   │   - Create audit log entry
│   │   - Update financial aggregates
│   │   - Notify involved parties
│   ├── Methods:
│   │   - void handle(Transaction transaction, TransactionService service)
│   │   - TransactionState getState()
│   └── Size: ~40 lines
│
├── RejectedTransactionState.java
│   ├── Type: @Component
│   ├── Implements: TransactionStateHandler
│   ├── Responsibilities:
│   │   - Log rejection reason
│   │   - Notify transaction creator
│   │   - Clean up temporary data
│   ├── Methods:
│   │   - void handle(Transaction transaction, TransactionService service)
│   │   - TransactionState getState()
│   └── Size: ~30 lines
│
└── TransactionStateManager.java
    ├── Type: @Service (Spring Bean)
    ├── Fields:
    │   - Map<TransactionState, TransactionStateHandler> stateHandlers (autowired)
    ├── Methods:
    │   - void processTransaction(Transaction transaction, TransactionService service)
    │   - void transitionState(Transaction transaction, TransactionState newState)
    │   - TransactionStateHandler getHandler(TransactionState state)
    │   - boolean canTransition(TransactionState from, TransactionState to)
    └── Size: ~50 lines
```

**Modified Files:**
- `src/main/java/com/shg/model/Transaction.java`
  - Add field: `@Enumerated(EnumType.STRING) private TransactionState state = TransactionState.PENDING;`
  - Add getter/setter for state
  - Add column definition if using JPA
  - Update `@Table` annotation for new column

- `src/main/java/com/shg/service/TransactionService.java`
  - Add `@Autowired TransactionStateManager stateManager`
  - Add method: `void approveTransaction(Long transactionId)`
  - Add method: `void rejectTransaction(Long transactionId, String rejectionReason)`
  - Modify `createTransaction()` to initialize state as PENDING and call stateManager
  - Add state transition logic

- `src/main/java/com/shg/controller/FinanceApiController.java` (or TransactionController)
  - Add `@Autowired TransactionService transactionService`
  - Add `@PostMapping("/transactions/{id}/approve")` endpoint
  - Add `@PostMapping("/transactions/{id}/reject")` endpoint with rejection reason
  - Add `@GetMapping("/transactions/{id}/state")` endpoint

---

### CONFIGURATION FILE

#### Location: `src/main/java/com/shg/config/`

```
config/
└── BehavioralPatternsConfig.java
    ├── Type: @Configuration (Spring)
    ├── Responsibilities:
    │   - Wire observers to TransactionService
    │   - Register state handlers in map
    │   - Configure strategy beans
    │   - Initialize command history management
    ├── Methods:
    │   - @Bean public BehavioralPatternsConfig behavioralPatternsConfig()
    │   - @Autowired void configureObservers(...)
    │   - @Bean public Map<TransactionState, TransactionStateHandler> stateHandlers(...)
    │   - @Bean public Map<String, InterestCalculationStrategy> strategies(...)
    └── Size: ~60-80 lines
```

---

## DETAILED IMPLEMENTATION CHECKLIST

### Phase 1: Directory Creation
```
☐ Create observer/ directory
☐ Create command/ directory
☐ Create strategy/ directory
☐ Create state/ directory
☐ Create config/ directory
```

### Phase 2: Observer Pattern
```
☐ Create BalanceObserver.java interface
☐ Create BalanceSubject.java interface
☐ Create DashboardObserver.java @Service
☐ Create ReportObserver.java @Service
☐ Create AuditObserver.java @Service
☐ Modify TransactionService.java to implement BalanceSubject
☐ Add observer notification calls in updateAggregates()
☐ Test with existing POST /api/transaction-records endpoint
```

### Phase 3: Command Pattern
```
☐ Create TransactionCommand.java interface
☐ Create CreateTransactionCommand.java class
☐ Create UpdateTransactionCommand.java class
☐ Create DeleteTransactionCommand.java class
☐ Create TransactionCommandInvoker.java @Service
☐ Modify TransactionController.java to use commandInvoker
☐ Add @PostMapping("/api/transaction-records/undo")
☐ Add @PostMapping("/api/transaction-records/redo")
☐ Add @GetMapping("/api/transaction-records/history")
☐ Test undo/redo with sample transactions
```

### Phase 4: Strategy Pattern
```
☐ Create InterestCalculationStrategy.java interface
☐ Create SimpleInterestStrategy.java @Component
☐ Create CompoundInterestStrategy.java @Component
☐ Create FinancialCalculationService.java @Service
☐ Modify AdvisoryService.java to use strategies
☐ Add interest calculation endpoints
☐ Test with different strategy parameters
```

### Phase 5: State Pattern
```
☐ Add TransactionState.java enum (in model/)
☐ Modify Transaction.java entity to add state field
☐ Create TransactionStateHandler.java interface
☐ Create PendingTransactionState.java @Component
☐ Create ApprovedTransactionState.java @Component
☐ Create RejectedTransactionState.java @Component
☐ Create TransactionStateManager.java @Service
☐ Modify TransactionService.java to use StateManager
☐ Add @PostMapping("/api/transactions/{id}/approve")
☐ Add @PostMapping("/api/transactions/{id}/reject")
☐ Add migration script for existing transactions
☐ Test state transitions
```

### Phase 6: Integration & Configuration
```
☐ Create BehavioralPatternsConfig.java
☐ Wire all observers in config
☐ Register all state handlers
☐ Configure strategy beans
☐ Run full application test
☐ Update PATTERNS.md with new patterns
☐ Add JavaDoc to all new classes
☐ Update README.md with architecture changes
```

---

## DATABASE MIGRATION (For State Pattern)

### SQL Script: `src/main/resources/sql/migration_v2_add_transaction_state.sql`

```sql
-- Add state column to transactions table
ALTER TABLE transactions 
ADD COLUMN state VARCHAR(20) DEFAULT 'PENDING' NOT NULL;

-- Add index for faster queries
CREATE INDEX idx_transaction_state ON transactions(state);

-- Update existing transactions if needed
UPDATE transactions SET state = 'APPROVED' WHERE created_at < NOW() - INTERVAL '1 hour';
```

### Update: `src/main/resources/sql/schema.sql`

Add to CREATE TABLE transactions:
```sql
state VARCHAR(20) NOT NULL DEFAULT 'PENDING',
rejection_reason VARCHAR(500)
```

---

## FILE SIZE ESTIMATES

| Category | Files | Avg Lines | Total |
|----------|-------|-----------|-------|
| Observer | 5 | 20 | 100 |
| Command | 5 | 40 | 200 |
| Strategy | 4 | 20 | 80 |
| State | 5 | 35 | 175 |
| Config | 1 | 70 | 70 |
| **Modifications** | 6 | 30 | 180 |
| **TOTAL** | 26 | - | **805** |

---

## DEPENDENCY INJECTION HIERARCHY

```
Spring Container
├── BehavioralPatternsConfig
│   ├── Wires Observers to TransactionService
│   ├── Provides StateHandlers Map
│   ├── Provides Strategy Map
│   └── Creates CommandInvoker Bean
│
├── TransactionService implements BalanceSubject
│   ├── Notifies: DashboardObserver, ReportObserver, AuditObserver
│   ├── Uses: TransactionStateManager
│   └── Called by: TransactionCommandInvoker
│
├── TransactionCommandInvoker @Service
│   ├── Manages: Create/Update/Delete commands
│   └── Uses: TransactionService
│
├── TransactionStateManager @Service
│   ├── Routes: Pending/Approved/Rejected states
│   └── Used by: TransactionService
│
└── FinancialCalculationService @Service
    ├── Uses: SimpleInterestStrategy, CompoundInterestStrategy
    └── Used by: AdvisoryService
```

---

## Next Steps After Reading This

1. **Create the 5 directories** in `src/main/java/com/shg/`
2. **Start with Observer Pattern** (least invasive, highest immediate benefit)
3. **Progress to Command/State/Strategy** in that order
4. **Update PATTERNS.md** after each pattern implementation
5. **Run tests** after each phase
6. **Commit to git** after each working pattern

