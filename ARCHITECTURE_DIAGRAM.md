# Architecture & Integration Diagram

## 🏗️ CURRENT PROJECT ARCHITECTURE

```
┌─────────────────────────────────────────────────────────────┐
│              SHG FINANCIAL TRACKER APPLICATION              │
└─────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                        CONTROLLERS (Web)                      │
├──────────────────────────────────────────────────────────────┤
│  WebController    |  TransactionController  |  FinanceAPI    │
│  (URL Mapping)    |  (REST CRUD)            |  (Business API)│
└──────────────────────────────────────────────────────────────┘
                           ↑ ↓
┌──────────────────────────────────────────────────────────────┐
│                    SERVICES (Business Logic)                  │
├──────────────────────────────────────────────────────────────┤
│  TransactionService  |  AdvisoryService  |  DashboardService │
│  MemberService       |  GroupService     |  ReportService    │
└──────────────────────────────────────────────────────────────┘
                           ↑ ↓
┌──────────────────────────────────────────────────────────────┐
│                    REPOSITORIES (Data Access)                 │
├──────────────────────────────────────────────────────────────┤
│  TransactionRepo  |  MemberRepo  |  GroupRepo  |  ReportRepo │
└──────────────────────────────────────────────────────────────┘
                           ↑ ↓
┌──────────────────────────────────────────────────────────────┐
│                      DATABASE (PostgreSQL)                    │
├──────────────────────────────────────────────────────────────┤
│  transactions | members | shg_groups | monthly_reports       │
└──────────────────────────────────────────────────────────────┘
```

---

## 🎯 ARCHITECTURE AFTER ADDING BEHAVIORAL PATTERNS

```
┌──────────────────────────────────────────────────────────────┐
│                        CONTROLLERS                            │
├──────────────────────────────────────────────────────────────┤
│  TransactionController (MODIFIED)                            │
│  ├─ Uses: CommandInvoker                                     │
│  ├─ Endpoints: /create, /undo, /redo, /history              │
│  └─ Endpoints: /approve, /reject                             │
└──────────────────────────────────────────────────────────────┘
                           ↓
┌──────────────────────────────────────────────────────────────┐
│                  COMMAND PATTERN LAYER                        │
├──────────────────────────────────────────────────────────────┤
│  TransactionCommandInvoker (@Service)                        │
│  ├─ CreateTransactionCommand (executes & undo)              │
│  ├─ UpdateTransactionCommand (executes & undo)              │
│  ├─ DeleteTransactionCommand (executes & undo)              │
│  └─ Manages: History Stack & Redo Stack                      │
└──────────────────────────────────────────────────────────────┘
                           ↓
┌──────────────────────────────────────────────────────────────┐
│                    SERVICES (MODIFIED)                        │
├──────────────────────────────────────────────────────────────┤
│  TransactionService (implements BalanceSubject)              │
│  ├─ Manages: observers list                                  │
│  ├─ Calls:   notifyBalanceChange()                          │
│  ├─ Uses:    TransactionStateManager                         │
│  └─ Methods: createTransaction, approveTransaction, etc.     │
└──────────────────────────────────────────────────────────────┘
       ↓                    ↓                    ↓
   ┌───────┐          ┌────────────┐      ┌──────────────┐
   │OBSERVER           │STATE PATTERN │   │STRATEGY PAT. │
   │PATTERN           │              │    │              │
   └───────┘          └────────────┘      └──────────────┘
       ↓                    ↓                    ↓
 ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
 │Dashboard    │    │Pending      │    │SimpleInt.   │
 │Observer     │    │State        │    │Strategy     │
 ├─────────────┤    ├─────────────┤    ├─────────────┤
 │Report       │    │Approved     │    │CompoundInt. │
 │Observer     │    │State        │    │Strategy     │
 ├─────────────┤    ├─────────────┤    ├─────────────┤
 │Audit        │    │Rejected     │    │Custom       │
 │Observer     │    │State        │    │Strategies   │
 └─────────────┘    └─────────────┘    └─────────────┘
```

---

## 🔗 PATTERN INTERACTION FLOW

### Observer Pattern Interaction
```
Client Request (POST /transaction)
        ↓
TransactionController
        ↓
CommandInvoker.executeCommand()
        ↓
CreateTransactionCommand.execute()
        ↓
TransactionService.createTransaction()
        ↓
TransactionService.updateAggregates()
        ↓
TransactionService.notifyBalanceChange()
        ├─→ DashboardObserver.onBalanceChanged() [Updates Dashboard]
        ├─→ ReportObserver.onBalanceChanged()    [Recalculates Reports]
        └─→ AuditObserver.onBalanceChanged()     [Logs Changes]
        ↓
Response to Client
```

### Command Pattern Interaction
```
POST /api/transaction-records
        ↓
TransactionController
        ↓
CommandInvoker.executeCommand(CreateTransactionCommand)
        ├─ Command.execute() → Creates transaction
        ├─ Push to history stack
        └─ Return created transaction
        ↓
Response: Transaction created
        ↓
POST /api/transaction-records/undo
        ↓
CommandInvoker.undoLastCommand()
        ├─ Pop from history stack
        ├─ Command.undo() → Deletes transaction
        └─ Push to redo stack
        ↓
Response: Transaction undone
```

### State Pattern Interaction
```
POST /api/transactions/{id}
        ↓
TransactionService.createTransaction()
        ├─ Set state = PENDING
        ├─ Save to DB
        └─ StateManager.process()
           ├─ Get PendingTransactionState handler
           ├─ handler.handle() → Validates, sends notification
           └─ Return saved transaction
        ↓
[Later] POST /api/transactions/{id}/approve
        ↓
TransactionService.approveTransaction()
        ├─ Set state = APPROVED
        ├─ StateManager.process()
        │  ├─ Get ApprovedTransactionState handler
        │  ├─ handler.handle() → Applies to balance
        │  ├─ Notifies observers
        │  └─ Creates audit entry
        └─ Update transaction state in DB
        ↓
Response: Transaction approved
```

### Strategy Pattern Interaction
```
GET /api/advisory/calculate-interest?principal=1000&strategy=simple
        ↓
AdvisoryService.calculateInterest()
        ↓
FinancialCalculationService.setStrategy("simple")
        ├─ strategies.get("simple") 
        └─ returns SimpleInterestStrategy instance
        ↓
FinancialCalculationService.calculateInterest(1000, 5, 12)
        ↓
SimpleInterestStrategy.calculate(1000, 5, 12)
        ├─ Formula: 1000 * 5 * 12
        └─ Result: 60000
        ↓
Response: { "interest": 60000, "principal": 1000, "total": 61000 }
```

---

## 📦 DEPENDENCY INJECTION WIRING

### BehavioralPatternsConfig.java (Central Wiring)
```java
@Configuration
public class BehavioralPatternsConfig {
    
    // Wire Observers to TransactionService
    @Autowired
    public void configureObservers(
        TransactionService transactionService,
        DashboardObserver dashboardObserver,
        ReportObserver reportObserver,
        AuditObserver auditObserver) {
        
        transactionService.addObserver(dashboardObserver);
        transactionService.addObserver(reportObserver);
        transactionService.addObserver(auditObserver);
    }
    
    // Provide State Handlers Map
    @Bean
    public Map<TransactionState, TransactionStateHandler> stateHandlers(
        PendingTransactionState pendingState,
        ApprovedTransactionState approvedState,
        RejectedTransactionState rejectedState) {
        
        Map<TransactionState, TransactionStateHandler> handlers = new HashMap<>();
        handlers.put(TransactionState.PENDING, pendingState);
        handlers.put(TransactionState.APPROVED, approvedState);
        handlers.put(TransactionState.REJECTED, rejectedState);
        return handlers;
    }
    
    // Provide Strategies Map
    @Bean
    public Map<String, InterestCalculationStrategy> strategies(
        SimpleInterestStrategy simpleStrategy,
        CompoundInterestStrategy compoundStrategy) {
        
        Map<String, InterestCalculationStrategy> strategies = new HashMap<>();
        strategies.put("simple", simpleStrategy);
        strategies.put("compound", compoundStrategy);
        return strategies;
    }
}
```

---

## 🔄 Class Relationships & Dependencies

### Observer Pattern Classes
```
                    ┌─────────────────────┐
                    │  BalanceObserver    │
                    │   (interface)       │
                    └─────────────────────┘
                           ↑ △ △
                     ┌──────┼─┼─┴─────┐
                     │      │ │       │
         ┌──────────────┐  ┌─┴──────────────┐  ┌──────────────┐
         │Dashboard     │  │  Report        │  │  Audit       │
         │Observer      │  │  Observer      │  │  Observer    │
         │(@Service)    │  │  (@Service)    │  │  (@Service)  │
         └──────────────┘  └─────────────────┘  └──────────────┘
                     △              △                  △
                     │              │                  │
             (implements)    (implements)        (implements)
                     │              │                  │
                     └──────────────┼──────────────────┘
                                    │
                    ┌───────────────────────────────┐
                    │  TransactionService           │
                    │  implements BalanceSubject    │
                    │  - observers : List           │
                    │  - addObserver()              │
                    │  - notifyBalanceChange()      │
                    └───────────────────────────────┘
```

### Command Pattern Classes
```
         ┌────────────────────────────┐
         │  TransactionCommand        │
         │  (interface)               │
         └────────────────────────────┘
                      ↑ △ △ △
        ┌─────────────┼─┼─┼─┘
        │             │ │
   ┌────────────┐  ┌──┴──────────┐  ┌─────────────┐
   │Create      │  │Update       │  │Delete       │
   │Transaction │  │Transaction  │  │Transaction  │
   │Command     │  │Command      │  │Command      │
   └────────────┘  └─────────────┘  └─────────────┘
        △               △                △
        │               │                │
        └───────────────┼────────────────┘
                        │
      ┌─────────────────────────────────┐
      │ TransactionCommandInvoker       │
      │ (@Service)                      │
      │ - commandHistory: Stack         │
      │ - redoStack: Stack              │
      │ - executeCommand()              │
      │ - undoLastCommand()             │
      │ - redoLastCommand()             │
      └─────────────────────────────────┘
           ↑
           │ commands stored in
           │
      ┌─────────────┐    ┌──────────────┐
      │History Stack│    │Redo Stack    │
      └─────────────┘    └──────────────┘
```

### State Pattern Classes
```
        ┌──────────────────────────┐
        │TransactionStateHandler   │
        │(interface)               │
        └──────────────────────────┘
                 ↑ △ △ △
         ┌───────┼─┼─┼─┘
         │       │ │
    ┌────────┐  ┌──────────┐  ┌──────────┐
    │Pending │  │Approved  │  │Rejected  │
    │State   │  │State     │  │State     │
    │Handler │  │Handler   │  │Handler   │
    └────────┘  └──────────┘  └──────────┘
         △          △              △
         │          │              │
         └──────────┼──────────────┘
                    │
      ┌─────────────────────────────┐
      │ TransactionStateManager     │
      │ (@Service)                  │
      │ - handlers: Map             │
      │ - processTransaction()      │
      │ - transitionState()         │
      └─────────────────────────────┘
           ↑
           │ contains handlers for
           │
      ┌──────────────────────────┐
      │ Transaction.state        │
      │ (enum: PENDING,          │
      │ APPROVED, REJECTED)      │
      └──────────────────────────┘
```

### Strategy Pattern Classes
```
    ┌─────────────────────────────┐
    │InterestCalculationStrategy  │
    │(interface)                  │
    └─────────────────────────────┘
              ↑ △ △
        ┌─────┼─┴─┐
        │     │   │
   ┌──────────────────┐  ┌──────────────────┐
   │SimpleInterest    │  │CompoundInterest  │
   │Strategy          │  │Strategy          │
   │(@Component)      │  │(@Component)      │
   └──────────────────┘  └──────────────────┘
        △                     △
        │                     │
        └─────────────────────┤──────────────┐
                              │              │
              ┌───────────────────────────┐  │
              │ FinancialCalculation      │  │
              │ Service (@Service)        │  │
              │ - strategies: Map         │◄─┘
              │ - setStrategy()           │
              │ - calculateInterest()     │
              └───────────────────────────┘
                      ↑
                      │ used by
                      │
              ┌───────────────┐
              │AdvisoryService│
              │(@Service)     │
              └───────────────┘
```

---

## 📊 Data Flow Diagram

### Complete Transaction Processing Flow
```
┌─ Client Request (Create Transaction) ─┐
│                                        │
└──────────────────┬─────────────────────┘
                   ↓
        ┌──────────────────────┐
        │ TransactionController│
        │ POST /api/trans/..   │
        └────────┬─────────────┘
                 ↓
        ┌──────────────────────────────┐
        │CommandInvoker                │
        │- executeCommand()            │
        └────────┬─────────────────────┘
                 ↓
        ┌──────────────────────────────┐
        │CreateTransactionCommand      │
        │- execute()                   │
        └────────┬─────────────────────┘
                 ↓
        ┌──────────────────────────────┐
        │TransactionService            │
        │- createTransaction()         │
        └────────┬─────────────────────┘
                 ↓
        ┌──────────────────────────────┐
        │TransactionStateManager       │
        │- processTransaction()        │
        │  [init state: PENDING]       │
        └────────┬─────────────────────┘
                 ↓
        ┌──────────────────────────────┐
        │PendingTransactionState       │
        │- handle() [validate, notify] │
        └────────┬─────────────────────┘
                 ↓
        ┌──────────────────────────────┐
        │TransactionService            │
        │- updateAggregates()          │
        └────────┬─────────────────────┘
                 ↓
    ┌────────────┴────────────┬─────────────┐
    ↓                         ↓             ↓
┌─────────────┐        ┌────────────┐  ┌──────────┐
│DashboardObs.│        │ReportObs.  │  │AuditObs. │
│onBalance..()│        │onBalance..()│  │onBalance()
└─────────────┘        └────────────┘  └──────────┘
    ↓                         ↓             ↓
[Update Cache]          [Recalculate]  [Log Audit]
    ↓                         ↓             ↓
    └────────────┬────────────┴─────────────┘
                 ↓
        ┌──────────────────────┐
        │Save to Repository    │
        └────────┬─────────────┘
                 ↓
        ┌──────────────────────┐
        │Return to Client      │
        │(Transaction created) │
        └──────────────────────┘
```

---

## 🎯 Where Each Pattern Interacts in the Code Flow

```
REQUEST FLOW:
User → Controller → Command → Service → StateManager → State Handler → Observers
                                                              ↓
                                                    [Observer Notifications]
                                                              ↓
                                                    Update Dashboard/Reports/Audit
                                                              ↓
                                                      Save to Database
                                                              ↓
                                                      Response to User

CALCULATION FLOW (For Recommendations):
Advisory Endpoint → AdvisoryService → FinancialCalculationService → Strategy
                                              ↓
                                    Strategy.calculate()
                                              ↓
                                      Return Result
                                              ↓
                                      Response to Client
```

---

## 📋 Implementation Dependency Order

```
Phase 1: Interfaces & Enums (Foundation)
├─ BalanceObserver ← BalanceSubject (Observer)
├─ TransactionCommand (Command)
├─ InterestCalculationStrategy (Strategy)
├─ TransactionStateHandler (State)
└─ TransactionState enum (State)

Phase 2: Concrete Implementations (Building blocks)
├─ DashboardObserver, ReportObserver, AuditObserver
├─ CreateTransactionCommand, UpdateTransactionCommand, DeleteTransactionCommand
├─ SimpleInterestStrategy, CompoundInterestStrategy
└─ PendingTransactionState, ApprovedTransactionState, RejectedTransactionState

Phase 3: Managers/Services (Orchestrators)
├─ TransactionCommandInvoker
├─ TransactionStateManager
├─ FinancialCalculationService
└─ Configuration (wire everything)

Phase 4: Integration (connect to existing)
├─ Modify TransactionService
├─ Modify TransactionController
├─ Modify FinanceApiController
├─ Modify Transaction entity
└─ Test complete flow
```

