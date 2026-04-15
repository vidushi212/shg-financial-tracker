# Quick Reference: Behavioral Patterns File Structure

## 🎯 QUICK VISUAL GUIDE

```
YOUR PROJECT CURRENTLY:
src/main/java/com/shg/
├── adapter/         ✓ EXISTS
├── builder/         ✓ EXISTS
├── controller/      ✓ EXISTS
├── facade/          ✓ EXISTS
├── factory/         ✓ EXISTS
├── model/           ✓ EXISTS
├── repository/      ✓ EXISTS
├── service/         ✓ EXISTS
└── view/            ✓ EXISTS

AFTER ADDING BEHAVIORAL PATTERNS:
src/main/java/com/shg/
├── adapter/         ✓ EXISTS
├── builder/         ✓ EXISTS
├── command/         ← CREATE (Behavioral)
├── config/          ← CREATE (Wiring)
├── controller/      ✓ EXISTS (MODIFY)
├── facade/          ✓ EXISTS
├── factory/         ✓ EXISTS
├── model/           ✓ EXISTS (MODIFY)
├── observer/        ← CREATE (Behavioral)
├── repository/      ✓ EXISTS
├── service/         ✓ EXISTS (MODIFY)
├── state/           ← CREATE (Behavioral)
├── strategy/        ← CREATE (Behavioral)
└── view/            ✓ EXISTS
```

---

## 📁 WHAT TO CREATE - DIRECTORY BY DIRECTORY

### **1️⃣ Observer Pattern Directory**
```
📦 observer/
├── 📄 BalanceObserver.java          ← Interface
├── 📄 BalanceSubject.java           ← Interface  
├── 📄 DashboardObserver.java        ← @Service Implementation
├── 📄 ReportObserver.java           ← @Service Implementation
└── 📄 AuditObserver.java            ← @Service Implementation

Location: src/main/java/com/shg/observer/
Total Files: 5
Total Lines: ~110
Purpose: Notify multiple components when balances change
```

### **2️⃣ Command Pattern Directory**
```
📦 command/
├── 📄 TransactionCommand.java               ← Interface
├── 📄 CreateTransactionCommand.java         ← Implementation
├── 📄 UpdateTransactionCommand.java         ← Implementation
├── 📄 DeleteTransactionCommand.java         ← Implementation
└── 📄 TransactionCommandInvoker.java        ← @Service Manager

Location: src/main/java/com/shg/command/
Total Files: 5
Total Lines: ~190
Purpose: Encapsulate operations and enable undo/redo
```

### **3️⃣ Strategy Pattern Directory**
```
📦 strategy/
├── 📄 InterestCalculationStrategy.java      ← Interface
├── 📄 SimpleInterestStrategy.java           ← Implementation
├── 📄 CompoundInterestStrategy.java         ← Implementation
└── 📄 FinancialCalculationService.java      ← @Service Context

Location: src/main/java/com/shg/strategy/
Total Files: 4
Total Lines: ~88
Purpose: Select different calculation algorithms dynamically
```

### **4️⃣ State Pattern Directory**
```
📦 state/
├── 📄 TransactionStateHandler.java          ← Interface
├── 📄 PendingTransactionState.java          ← Implementation
├── 📄 ApprovedTransactionState.java         ← Implementation
├── 📄 RejectedTransactionState.java         ← Implementation
└── 📄 TransactionStateManager.java          ← @Service Manager

Location: src/main/java/com/shg/state/
Total Files: 5
Total Lines: ~180
Purpose: Handle transaction state transitions and behaviors
```

### **5️⃣ Configuration Directory**
```
📦 config/
└── 📄 BehavioralPatternsConfig.java         ← @Configuration

Location: src/main/java/com/shg/config/
Total Files: 1
Total Lines: ~70
Purpose: Wire beans and dependencies for all patterns
```

---

## 🔄 WHAT TO MODIFY - FILES TO CHANGE

| File | Change Type | Impact | Difficulty |
|------|------------|--------|------------|
| **TransactionService.java** | Add `implements BalanceSubject` + observers | HIGH | MEDIUM |
| **TransactionController.java** | Inject `commandInvoker`, add new endpoints | HIGH | EASY |
| **Transaction.java** | Add state field + state property | MEDIUM | EASY |
| **FinanceApiController.java** | Add state endpoints | MEDIUM | EASY |
| **AdvisoryService.java** | Inject strategy, add calculation methods | MEDIUM | EASY |

---

## 📋 STEP-BY-STEP CHECKLIST WITH PATHS

### **STEP 1: Create Directories** (5 minutes)
```
☐ mkdir src/main/java/com/shg/observer
☐ mkdir src/main/java/com/shg/command
☐ mkdir src/main/java/com/shg/strategy
☐ mkdir src/main/java/com/shg/state
☐ mkdir src/main/java/com/shg/config
```

### **STEP 2: Create Observer Files** (20 minutes)
```
☐ src/main/java/com/shg/observer/BalanceObserver.java
  └─ Copy interface template from guide
  
☐ src/main/java/com/shg/observer/BalanceSubject.java
  └─ Copy interface template from guide
  
☐ src/main/java/com/shg/observer/DashboardObserver.java
  └─ Add @Service annotation
  └─ Implement BalanceObserver interface
  
☐ src/main/java/com/shg/observer/ReportObserver.java
  └─ Add @Service annotation
  └─ Implement BalanceObserver interface
  
☐ src/main/java/com/shg/observer/AuditObserver.java
  └─ Add @Service annotation
  └─ Implement BalanceObserver interface
```

### **STEP 3: Modify TransactionService** (15 minutes)
```
☐ Edit src/main/java/com/shg/service/TransactionService.java
  ├─ Add: implements BalanceSubject
  ├─ Add: private List<BalanceObserver> observers = new ArrayList<>()
  ├─ Add: Methods from BalanceSubject interface
  └─ Modify: updateAggregates() to call notify methods
```

### **STEP 4: Create Command Files** (30 minutes)
```
☐ src/main/java/com/shg/command/TransactionCommand.java
  └─ Copy interface template
  
☐ src/main/java/com/shg/command/CreateTransactionCommand.java
  └─ Implement execute() and undo()
  
☐ src/main/java/com/shg/command/UpdateTransactionCommand.java
  └─ Implement execute() and undo()
  
☐ src/main/java/com/shg/command/DeleteTransactionCommand.java
  └─ Implement execute() and undo()
  
☐ src/main/java/com/shg/command/TransactionCommandInvoker.java
  └─ Add undo/redo stack management
```

### **STEP 5: Modify TransactionController** (15 minutes)
```
☐ Edit src/main/java/com/shg/controller/TransactionController.java
  ├─ Add: @Autowired TransactionCommandInvoker commandInvoker
  ├─ Modify: createTransaction() to use commands
  ├─ Add: @PostMapping("/undo")
  ├─ Add: @PostMapping("/redo")
  └─ Add: @GetMapping("/history")
```

### **STEP 6: Create Strategy Files** (20 minutes)
```
☐ src/main/java/com/shg/strategy/InterestCalculationStrategy.java
  └─ Copy interface template
  
☐ src/main/java/com/shg/strategy/SimpleInterestStrategy.java
  └─ Formula: I = P × R × T
  
☐ src/main/java/com/shg/strategy/CompoundInterestStrategy.java
  └─ Formula: A = P(1 + r)^t
  
☐ src/main/java/com/shg/strategy/FinancialCalculationService.java
  └─ Strategy selection and execution logic
```

### **STEP 7: Modify Transaction Model** (10 minutes)
```
☐ Edit src/main/java/com/shg/model/Transaction.java
  ├─ Add: enum TransactionState (or in separate file)
  ├─ Add: @Enumerated(EnumType.STRING) private TransactionState state
  └─ Add: getters/setters for state field
```

### **STEP 8: Create State Files** (30 minutes)
```
☐ src/main/java/com/shg/state/TransactionStateHandler.java
  └─ Copy interface template
  
☐ src/main/java/com/shg/state/PendingTransactionState.java
  └─ Implement pending state behavior
  
☐ src/main/java/com/shg/state/ApprovedTransactionState.java
  └─ Implement approved state behavior
  
☐ src/main/java/com/shg/state/RejectedTransactionState.java
  └─ Implement rejected state behavior
  
☐ src/main/java/com/shg/state/TransactionStateManager.java
  └─ State routing and management logic
```

### **STEP 9: Create Configuration** (15 minutes)
```
☐ src/main/java/com/shg/config/BehavioralPatternsConfig.java
  ├─ Add: @Configuration annotation
  ├─ Wire: observers to TransactionService
  ├─ Wire: state handlers map
  ├─ Wire: strategies map
  └─ Wire: command invoker beans
```

### **STEP 10: Test & Verify** (30+ minutes)
```
☐ mvn clean compile
☐ mvn spring-boot:run
☐ Test observer notifications
☐ Test command undo/redo
☐ Test strategy calculations
☐ Test state transitions
☐ Debug and fix issues
```

---

## 🎨 QUICK CODE SNIPPETS REFERENCE

### Observer Pattern - Quick Template
```java
// Interface
public interface BalanceObserver {
    void onBalanceChanged(SHGGroup group, double oldBalance, double newBalance);
}

// Subject in TransactionService
implements BalanceSubject {
    private List<BalanceObserver> observers = new ArrayList<>();
    
    @Override
    public void addObserver(BalanceObserver observer) {
        observers.add(observer);
    }
    // ... more methods
}

// Service Implementation
@Service
public class DashboardObserver implements BalanceObserver {
    @Override
    public void onBalanceChanged(SHGGroup group, double oldBalance, double newBalance) {
        // Update dashboard
    }
}
```

### Command Pattern - Quick Template
```java
// Interface
public interface TransactionCommand {
    void execute();
    void undo();
    String getDescription();
}

// Implementation
public class CreateTransactionCommand implements TransactionCommand {
    private TransactionService service;
    private Transaction transaction;
    private Transaction created;
    
    @Override
    public void execute() {
        created = service.createTransaction(transaction);
    }
    
    @Override
    public void undo() {
        service.deleteTransaction(created.getId());
    }
}

// Invoker
@Service
public class TransactionCommandInvoker {
    private Stack<TransactionCommand> history = new Stack<>();
    
    public void executeCommand(TransactionCommand cmd) {
        cmd.execute();
        history.push(cmd);
    }
}
```

### Strategy Pattern - Quick Template
```java
// Interface
public interface InterestCalculationStrategy {
    double calculate(double principal, double rate, int time);
}

// Implementations
@Component("simple")
public class SimpleInterestStrategy implements InterestCalculationStrategy {
    @Override
    public double calculate(double principal, double rate, int time) {
        return principal * rate * time;
    }
}

// Service
@Service
public class FinancialCalculationService {
    private Map<String, InterestCalculationStrategy> strategies;
    private InterestCalculationStrategy current;
    
    public void setStrategy(String name) {
        current = strategies.get(name);
    }
}
```

### State Pattern - Quick Template
```java
// Handler Interface
public interface TransactionStateHandler {
    void handle(Transaction transaction, TransactionService service);
    TransactionState getState();
}

// Implementations
@Component
public class ApprovedTransactionState implements TransactionStateHandler {
    @Override
    public void handle(Transaction transaction, TransactionService service) {
        // Apply transaction logic
    }
}

// Manager
@Service
public class TransactionStateManager {
    private Map<TransactionState, TransactionStateHandler> handlers;
    
    public void process(Transaction transaction) {
        handlers.get(transaction.getState())
                .handle(transaction, service);
    }
}
```

---

## 📊 IMPLEMENTATION TIMELINE

```
Day 1  : Observer Pattern (directories + files)     [2 hours]
Day 2  : Observer Integration + Testing              [2 hours]
Day 3  : Command Pattern (directories + files)       [2 hours]
Day 4  : Command Integration + Testing               [2 hours]
Day 5  : Strategy Pattern (directories + files)      [1.5 hours]
Day 6  : Strategy Integration + Testing              [1.5 hours]
Day 7  : State Pattern (directories + files)         [2 hours]
Day 8  : State Integration + Testing + Config        [2 hours]
Day 9  : Full System Testing                         [2 hours]
Day 10 : Documentation & Cleanup                     [1 hour]
         TOTAL: ~17.5 hours
```

---

## 🚀 DO THIS FIRST (Get Started Immediately)

### Created Files - Copy These Commands:

```bash
# 1. Create directories
cd src/main/java/com/shg
mkdir observer command strategy state config

# 2. Create Observer interfaces first
# File: observer/BalanceObserver.java
# File: observer/BalanceSubject.java

# 3. Test your setup
mvn clean compile

# 4. Then proceed with implementations
```

---

## ❓ FREQUENTLY ASKED QUESTIONS

**Q: Do I need to create all patterns at once?**
A: No! Start with Observer (simplest), then add others incrementally.

**Q: Which files get @Service annotation?**
A: Your concrete implementations (DashboardObserver, CommandInvoker, etc.)

**Q: Do I modify existing files a lot?**
A: Minimally - mainly TransactionController and TransactionService.

**Q: Where do I put the new model changes (state)?**
A: In Transaction.java model file, add `@Enumerated TransactionState state` field.

**Q: How do I verify implementation is correct?**
A: The project will compile (mvn clean compile) if structure is correct.

**Q: Can I skip any pattern?**
A: Yes! Each is independent. But Observer is recommended as first step.

---

## 📱 FILE COUNT SUMMARY

| Pattern | NEW Files | MODIFIED Files | Total Changes |
|---------|-----------|-----------------|-----------------|
| Observer | 5 | 1 | 6 |
| Command | 5 | 2 | 7 |
| Strategy | 4 | 1 | 5 |
| State | 5 | 3 | 8 |
| Config | 1 | 0 | 1 |
| **TOTAL** | **20** | **7** | **27** |

**Total New Code: ~800 lines**

---

## 🎓 REMEMBER

✅ Create directories first
✅ Create interfaces before implementations
✅ Use @Service for Spring components
✅ Modify existing services minimally
✅ Test each pattern as you go
✅ Update PATTERNS.md after each phase

