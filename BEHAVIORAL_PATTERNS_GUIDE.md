# Behavioral Patterns Implementation Guide

## Project Structure After Adding Patterns

```
src/main/java/com/shg/
├── SHGFinancialTrackerApplication.java
├── WebController.java
├── DemoDataLoader.java
│
├── adapter/          (EXISTING - Structural Pattern)
├── builder/          (EXISTING - Creational Pattern)
├── facade/           (EXISTING - Structural Pattern)
├── factory/          (EXISTING - Creational Pattern)
├── controller/       (EXISTING)
├── model/            (EXISTING)
├── repository/       (EXISTING)
├── service/          (EXISTING)
├── view/             (EXISTING)
│
├── observer/         (NEW - Behavioral Pattern #1)
│   ├── BalanceObserver.java
│   ├── BalanceSubject.java
│   ├── DashboardObserver.java
│   ├── ReportObserver.java
│   └── AuditObserver.java
│
├── command/          (NEW - Behavioral Pattern #2)
│   ├── TransactionCommand.java
│   ├── CreateTransactionCommand.java
│   ├── DeleteTransactionCommand.java
│   ├── UpdateTransactionCommand.java
│   └── TransactionCommandInvoker.java
│
├── strategy/         (NEW - Behavioral Pattern #3)
│   ├── InterestCalculationStrategy.java
│   ├── SimpleInterestStrategy.java
│   ├── CompoundInterestStrategy.java
│   └── FinancialCalculationService.java
│
├── state/            (NEW - Behavioral Pattern #4)
│   ├── TransactionStateHandler.java
│   ├── PendingTransactionState.java
│   ├── ApprovedTransactionState.java
│   ├── RejectedTransactionState.java
│   └── TransactionStateManager.java
│
└── config/           (NEW - Configuration)
    └── BehavioralPatternsConfig.java
```

---

## PATTERN #1: Observer Pattern

### **Files to Create:**

#### 1. `src/main/java/com/shg/observer/BalanceObserver.java`
```
NEW FILE - Observer interface
Purpose: Define contract for objects that listen to balance changes
```

#### 2. `src/main/java/com/shg/observer/BalanceSubject.java`
```
NEW FILE - Subject interface
Purpose: Define contract for objects that notify observers
```

#### 3. `src/main/java/com/shg/observer/DashboardObserver.java`
```
NEW FILE - Concrete observer implementation
Purpose: Update dashboard cache when balances change
```

#### 4. `src/main/java/com/shg/observer/ReportObserver.java`
```
NEW FILE - Concrete observer implementation
Purpose: Trigger report recalculation on balance changes
```

#### 5. `src/main/java/com/shg/observer/AuditObserver.java`
```
NEW FILE - Concrete observer implementation
Purpose: Log all balance changes for audit trail
```

### **Files to Modify:**

#### Modify: `src/main/java/com/shg/service/TransactionService.java`
```
- Add implements BalanceSubject
- Add List<BalanceObserver> observers field
- Add observer management methods
- Modify updateAggregates() to call notifyBalanceChange()
```

---

## PATTERN #2: Command Pattern

### **Files to Create:**

#### 1. `src/main/java/com/shg/command/TransactionCommand.java`
```
NEW FILE - Command interface
Purpose: Define contract for executable commands
```

#### 2. `src/main/java/com/shg/command/CreateTransactionCommand.java`
```
NEW FILE - Concrete command
Purpose: Encapsulate create transaction operation
```

#### 3. `src/main/java/com/shg/command/UpdateTransactionCommand.java`
```
NEW FILE - Concrete command
Purpose: Encapsulate update transaction operation
```

#### 4. `src/main/java/com/shg/command/DeleteTransactionCommand.java`
```
NEW FILE - Concrete command
Purpose: Encapsulate delete transaction operation
```

#### 5. `src/main/java/com/shg/command/TransactionCommandInvoker.java`
```
NEW FILE - Command invoker/manager
Purpose: Execute commands and manage undo/redo history
```

### **Files to Modify:**

#### Modify: `src/main/java/com/shg/controller/TransactionController.java`
```
- Inject TransactionCommandInvoker
- Replace direct service calls with command execution
- Add /undo endpoint
- Add /redo endpoint
- Add /command-history endpoint
```

#### Modify: `src/main/java/com/shg/service/TransactionService.java`
```
- Add deleteTransaction() method (if not exists)
- Add updateTransaction() method
- Add applyTransactionToBalances() method
```

---

## PATTERN #3: Strategy Pattern

### **Files to Create:**

#### 1. `src/main/java/com/shg/strategy/InterestCalculationStrategy.java`
```
NEW FILE - Strategy interface
Purpose: Define contract for interest calculation algorithms
```

#### 2. `src/main/java/com/shg/strategy/SimpleInterestStrategy.java`
```
NEW FILE - Concrete strategy
Purpose: Calculate simple interest
Formula: I = P × R × T
```

#### 3. `src/main/java/com/shg/strategy/CompoundInterestStrategy.java`
```
NEW FILE - Concrete strategy
Purpose: Calculate compound interest
Formula: A = P(1 + r/n)^(nt)
```

#### 4. `src/main/java/com/shg/strategy/FinancialCalculationService.java`
```
NEW FILE - Context/Strategy manager
Purpose: Select and apply strategies for calculations
```

### **Files to Modify:**

#### Modify: `src/main/java/com/shg/service/AdvisoryService.java` (or create new)
```
- Inject FinancialCalculationService
- Use strategies for investment projections
- Use strategies for scheme recommendations
```

---

## PATTERN #4: State Pattern

### **Files to Create:**

#### 1. `src/main/java/com/shg/state/TransactionStateHandler.java`
```
NEW FILE - State interface
Purpose: Define contract for different transaction states
```

#### 2. `src/main/java/com/shg/state/PendingTransactionState.java`
```
NEW FILE - Concrete state
Purpose: Handle pending transaction behavior
Actions: Send notifications, validate data
```

#### 3. `src/main/java/com/shg/state/ApprovedTransactionState.java`
```
NEW FILE - Concrete state
Purpose: Handle approved transaction behavior
Actions: Apply to balances, create audit log
```

#### 4. `src/main/java/com/shg/state/RejectedTransactionState.java`
```
NEW FILE - Concrete state
Purpose: Handle rejected transaction behavior
Actions: Log rejection reason, notify user
```

#### 5. `src/main/java/com/shg/state/TransactionStateManager.java`
```
NEW FILE - State manager
Purpose: Manage state transitions and handler execution
```

### **Files to Modify:**

#### Modify: `src/main/java/com/shg/model/Transaction.java`
```
- Add TransactionState enum (or in separate file under model/)
- Add state field to Transaction entity
- Initialize default state as PENDING
```

#### Modify: `src/main/java/com/shg/service/TransactionService.java`
```
- Inject TransactionStateManager
- Add approveTransaction(Long id) method
- Add rejectTransaction(Long id, String reason) method
- Add state transition method
```

#### Modify: `src/main/java/com/shg/controller/FinanceApiController.java`
```
- Add @PostMapping("/approve/{id}") endpoint
- Add @PostMapping("/reject/{id}") endpoint
```

---

## CONFIGURATION FILE

### **Create: `src/main/java/com/shg/config/BehavioralPatternsConfig.java`**
```
NEW FILE - Configuration class
Purpose: Wire up all observers, strategies, and state handlers
Location: src/main/java/com/shg/config/
Responsibility: Initialize beans and dependency injection
```

---

## STEP-BY-STEP IMPLEMENTATION ORDER

### **Phase 1: Create Directory Structure**
```
1. Create observer/
2. Create command/
3. Create strategy/
4. Create state/
5. Create config/
```

### **Phase 2: Implement Observer Pattern (Days 1-2)**
```
1. Create observation interfaces
2. Create concrete observers
3. Modify TransactionService
4. Add BehavioralPatternsConfig
5. Test with existing endpoints
```

### **Phase 3: Implement Command Pattern (Days 3-4)**
```
1. Create command interfaces
2. Create concrete commands
3. Create CommandInvoker
4. Modify TransactionController
5. Add /undo, /redo endpoints
```

### **Phase 4: Implement Strategy Pattern (Days 5-6)**
```
1. Create strategy interfaces
2. Create concrete strategies
3. Create FinancialCalculationService
4. Integrate with AdvisoryService
5. Test calculations
```

### **Phase 5: Implement State Pattern (Days 7-8)**
```
1. Add state enum to Transaction model
2. Create state interfaces
3. Create concrete states
4. Create StateManager
5. Add approval endpoints
6. Test state transitions
```

---

## QUICK REFERENCE: What Gets Modified vs Created

| Pattern | New Files | Modified Files |
|---------|-----------|-----------------|
| Observer | 5 new files | TransactionService.java |
| Command | 5 new files | TransactionController.java, TransactionService.java |
| Strategy | 4 new files | AdvisoryService.java (or new) |
| State | 5 new files | Transaction.java, TransactionService.java, FinanceApiController.java |
| Config | 1 new file | (Spring will auto-scan) |

---

## DIRECTORY CREATION COMMANDS (PowerShell)

```powershell
# Navigate to src/main/java/com/shg
cd src\main\java\com\shg

# Create new pattern directories
New-Item -ItemType Directory -Path "observer" -Force
New-Item -ItemType Directory -Path "command" -Force
New-Item -ItemType Directory -Path "strategy" -Force
New-Item -ItemType Directory -Path "state" -Force
New-Item -ItemType Directory -Path "config" -Force
```

---

## FILES OVERVIEW TABLE

| File Path | Type | Lines | Purpose |
|-----------|------|-------|---------|
| observer/BalanceObserver.java | Interface | 3-5 | Observer contract |
| observer/BalanceSubject.java | Interface | 5-7 | Subject contract |
| observer/DashboardObserver.java | Implementation | 15-20 | Observes dashboard |
| observer/ReportObserver.java | Implementation | 15-20 | Observes reports |
| observer/AuditObserver.java | Implementation | 15-20 | Observes audit |
| command/TransactionCommand.java | Interface | 3-5 | Command contract |
| command/CreateTransactionCommand.java | Implementation | 30-40 | Create operation |
| command/UpdateTransactionCommand.java | Implementation | 30-40 | Update operation |
| command/DeleteTransactionCommand.java | Implementation | 30-40 | Delete operation |
| command/TransactionCommandInvoker.java | Implementation | 40-50 | Command manager |
| strategy/InterestCalculationStrategy.java | Interface | 3-5 | Strategy contract |
| strategy/SimpleInterestStrategy.java | Implementation | 10-15 | Simple interest calc |
| strategy/CompoundInterestStrategy.java | Implementation | 10-15 | Compound interest calc |
| strategy/FinancialCalculationService.java | Implementation | 30-40 | Strategy manager |
| state/TransactionStateHandler.java | Interface | 3-5 | State contract |
| state/PendingTransactionState.java | Implementation | 20-30 | Pending state |
| state/ApprovedTransactionState.java | Implementation | 20-30 | Approved state |
| state/RejectedTransactionState.java | Implementation | 20-30 | Rejected state |
| state/TransactionStateManager.java | Implementation | 30-40 | State manager |
| config/BehavioralPatternsConfig.java | Configuration | 50-70 | Bean configuration |

---

## SUMMARY

**Total New Files to Create**: 19 files  
**Total Existing Files to Modify**: 5-6 files  
**New Directories**: 5 directories  
**Estimated Total Lines of Code**: ~500-600 lines

