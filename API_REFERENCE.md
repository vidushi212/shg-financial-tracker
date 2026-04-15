# Quick Developer Reference - Behavioral Patterns API

## 🚀 Quick Start

### Build & Run
```bash
mvn clean compile      # Verify all 84 files compile
mvn spring-boot:run    # Start the application
```

### Check Console Output
Watch for these messages indicating patterns are working:
```
=== Configuring Observers ===
✓ DashboardObserver registered
✓ ReportObserver registered
✓ AuditObserver registered

=== Configuring State Handlers ===
✓ PendingTransactionState registered
✓ ApprovedTransactionState registered
✓ RejectedTransactionState registered

=== Configuring Strategies ===
✓ SimpleInterestStrategy registered
✓ CompoundInterestStrategy registered
```

---

## 🔐 Role-Based Endpoints

### Create Transaction (Any authorized role)
```http
POST /api/transaction-records

Content-Type: application/json

{
  "type": "SAVINGS",
  "amount": 1000.00,
  "description": "Monthly savings",
  "recordedBy": "user1",
  "shgGroupId": 1,
  "memberId": 5
}

Response:
{
  "id": 123,
  "type": "SAVINGS",
  "amount": 1000.00,
  "state": "PENDING",
  "recordedBy": "user1",
  "createdAt": "2026-04-15T10:30:45",
  "status": "Transaction created - awaiting approval"
}
```

### Approve Transaction (ACCOUNTANT, TREASURER, PRESIDENT only)
```http
POST /api/transactions/123/approve

Content-Type: application/json

{
  "approverUserId": 1,
  "approverRole": "ACCOUNTANT"
}

Response:
{
  "status": "success",
  "message": "Transaction approved successfully",
  "transactionId": 123,
  "approvedBy": "John Accountant",
  "transactionState": "APPROVED"
}
```

### Reject Transaction (ACCOUNTANT, TREASURER, PRESIDENT only)
```http
POST /api/transactions/123/reject

Content-Type: application/json

{
  "rejectorUserId": 1,
  "rejectorRole": "ACCOUNTANT",
  "reason": "Amount exceeds daily limit"
}

Response:
{
  "status": "success",
  "message": "Transaction rejected successfully",
  "transactionId": 123,
  "rejectedBy": "John Accountant",
  "rejectionReason": "Amount exceeds daily limit",
  "transactionState": "REJECTED"
}
```

### Check Transaction State
```http
GET /api/transactions/123/state?userRole=ACCOUNTANT

Response:
{
  "transactionId": 123,
  "currentState": "PENDING",
  "availableActions": ["APPROVE", "REJECT"]
}
```

### Get Pending Transactions (Role-filtered)
```http
GET /api/transactions/pending?userRole=ACCOUNTANT

Response:
{
  "role": "ACCOUNTANT",
  "canApprove": true,
  "pendingCount": 5,
  "transactions": [
    {
      "id": 123,
      "type": "SAVINGS",
      "amount": 1000.00,
      "memberName": "Jane Member",
      "createdAt": "2026-04-15T10:30:00",
      "state": "PENDING"
    }
  ]
}
```

---

## 💰 Strategy Pattern - Interest Calculations

### Simple Interest
```http
POST /api/advisory/calculate-interest

Content-Type: application/json

{
  "principal": 10000,
  "annualRate": 0.05,
  "timeInMonths": 12,
  "strategy": "simpleInterest",
  "userId": 1,
  "userRole": "ACCOUNTANT"
}

Response:
{
  "strategy": "Simple Interest",
  "principal": 10000,
  "annualRate": "5%",
  "timeInMonths": 12,
  "interest": 500.00,
  "totalAmount": 10500.00,
  "formula": "I = P × R × T",
  "executedBy": "John Accountant"
}

Calculation: 10000 × 0.05 × 1 = 500
```

### Compound Interest
```http
POST /api/advisory/calculate-interest

Content-Type: application/json

{
  "principal": 10000,
  "annualRate": 0.05,
  "timeInMonths": 12,
  "strategy": "compoundInterest",
  "userId": 1,
  "userRole": "ACCOUNTANT"
}

Response:
{
  "strategy": "Compound Interest",
  "principal": 10000,
  "annualRate": "5%",
  "timeInMonths": 12,
  "interest": 511.39,
  "totalAmount": 10511.39,
  "formula": "A = P(1 + r/n)^(nt) - Compounded Monthly",
  "executedBy": "John Accountant"
}

Calculation: 10000 × (1 + 0.05/12)^12 - 10000 = 511.39
```

---

## 🔄 Command Pattern - Undo/Redo

### Undo Last Command
```http
POST /api/transaction-records/undo

Header:
X-User-Id: 1
X-User-Role: ACCOUNTANT

Response:
{
  "status": "success",
  "message": "Command undone: Create transaction: SAVINGS - Amount: 1000.00",
  "previousState": "APPROVED",
  "newState": "UNDONE"
}
```

### Redo Last Command
```http
POST /api/transaction-records/redo

Header:
X-User-Id: 1
X-User-Role: ACCOUNTANT

Response:
{
  "status": "success",
  "message": "Command redone: Create transaction: SAVINGS - Amount: 1000.00",
  "state": "APPROVED"
}
```

### View Command History
```http
GET /api/transaction-records/history?userRole=PRESIDENT

Response:
{
  "role": "PRESIDENT",
  "historyCount": 15,
  "history": [
    "[2026-04-15 10:30:45] Create transaction: SAVINGS - Amount: 1000.00 (by John User - ACCOUNTANT)",
    "[2026-04-15 10:25:30] Create transaction: EXPENSE - Amount: 500.00 (by Jane Secretary - SECRETARY)",
    "[2026-04-15 10:15:00] Create transaction: LOAN - Amount: 2000.00 (by John User - ACCOUNTANT)"
  ]
}
```

---

## 📊 Role-Based Access Examples

### President View (Full Access)
```java
// President can see everything
PRESIDENT perspective:
├─ All transactions (created, pending, approved, rejected)
├─ All members' financial data
├─ Complete audit trail with all user actions
├─ Can approve/reject/delete any transaction
├─ Can undo any user's commands
├─ Full system monitoring and analytics
└─ Administrative functions available
```

### Accountant View (Financial Management)
```java
// Accountant manages finances
ACCOUNTANT perspective:
├─ All transactions (but not delete approved ones)
├─ Only own actions in audit trail
├─ Can approve/reject pending transactions
├─ Full access to all calculation strategies
├─ Can generate financial reports
└─ Dashboard showing financial metrics
```

### Secretary View (Member Management)
```java
// Secretary manages members
SECRETARY perspective:
├─ Own transactions only
├─ Member management interface
├─ Group reports (summary only)
├─ Limited to simple interest calculations
├─ Can create transactions (requires approval)
└─ Cannot see detailed financial dashboards
```

### Member View (Own Data Only)
```java
// Members have minimal access
MEMBER perspective:
├─ Own transaction history
├─ Own savings and loan accounts
├─ Cannot create transactions directly
├─ Cannot access calculation tools
├─ Cannot see other members' data
└─ Cannot access admin functions
```

---

## ⏩ Common Workflows

### Workflow 1: Create & Approve Transaction
```
1. Member initiates transaction
   POST /api/transaction-records
   → State: PENDING
   → Observers notified: Transaction pending

2. Accountant reviews
   GET /api/transactions/pending?userRole=ACCOUNTANT
   → Shows all pending transactions

3. Accountant approves
   POST /api/transactions/{id}/approve
   → State: APPROVED
   → Observers notified: DashboardObserver updates balance
   → Observers notified: ReportObserver regenerates reports
   → Observers notified: AuditObserver logs approval

4. System updated
   → Group balance changed
   → Member savings/loan updated
   → Real-time dashboard updates
   → Report cache invalidated
```

### Workflow 2: Calculate Interest for Loan
```
1. Accountant needs projection
   POST /api/advisory/calculate-interest
   {
     "principal": 50000,
     "annualRate": 0.08,
     "strategy": "compoundInterest"
   }

2. System calculates
   → Uses CompoundInterestStrategy
   → Checks ACCOUNTANT permission (✓ Allowed)
   → Returns monthly compounded interest

3. Result shown
   → Interest: 4128.73
   → Total after 12 months: 54128.73
```

### Workflow 3: Undo Mistaken Transaction
```
1. Member creates wrong transaction
   POST /api/transaction-records

2. Accountant realizes error
   POST /api/transaction-records/undo
   → Uses CreateTransactionCommand
   → Reverses balance changes
   → Recorded in audit log

3. System restored
   → Transaction deleted
   → Balance reversed
   → Reports recalculated
   → History shows undo action
```

---

## 🐛 Debugging Tips

### Check Observers Are Registered
```
Look for console output:
✓ DashboardObserver registered
✓ ReportObserver registered
✓ AuditObserver registered
```

### View Audit Trail
- PRESIDENT can see all actions
- Other roles see only their own actions
- Look for "AUDIT LOG" messages in console

### Check Role Permissions
```java
// Use RoleAuthorizationService to verify
RoleAuthorizationService.canApproveTransactions(member)
RoleAuthorizationService.canCreateTransaction(member)
RoleAuthorizationService.canViewAllTransactions(member)
```

### Monitor Transaction State
```
States: PENDING → (APPROVED | REJECTED)
- PENDING: Created, awaiting approval
- APPROVED: Applied to balances (final)
- REJECTED: Not applied (can resubmit)
```

### Check Strategy Availability
```
PRESIDENT/ACCOUNTANT: [ simpleInterest, compoundInterest ]
Others: [ simpleInterest only ]
```

---

## 📝 Error Responses

### Unauthorized Role
```json
{
  "error": "User does not have permission to approve transactions",
  "status": 403,
  "role": "MEMBER",
  "required": "ACCOUNTANT, TREASURER, or PRESIDENT"
}
```

### Invalid State Transition
```json
{
  "error": "Cannot approve: Transaction is not in PENDING state",
  "current": "APPROVED",
  "requested": "APPROVED"
}
```

### Unknown Strategy
```json
{
  "error": "Unknown strategy: compoundQuarterly. Available: simpleInterest, compoundInterest"
}
```

### Transaction Not Found
```json
{
  "error": "Transaction not found",
  "id": 9999,
  "status": 404
}
```

---

## ✅ Testing Checklist

- [ ] Create transaction as SECRETARY
- [ ] Approve as ACCOUNTANT
- [ ] Check dashboard updated
- [ ] View audit log as PRESIDENT
- [ ] Calculate simple interest as ACCOUNTANT
- [ ] Try compound interest as MEMBER (should fail)
- [ ] Undo transaction command
- [ ] Check observer notifications in console
- [ ] Reject transaction with reason
- [ ] Verify state transitions

---

## 📞 Reference Links

- **Implementation Doc**: `IMPLEMENTATION_COMPLETE.md`
- **Architecture Guide**: `ARCHITECTURE_DIAGRAM.md`
- **File Structure**: `DETAILED_FILE_LOCATIONS.md`
- **API Endpoints**: This file
- **Patterns Guide**: `BEHAVIORAL_PATTERNS_GUIDE.md`

