package com.shg.service;

import com.shg.model.MonthlyReport;
import com.shg.model.SHGGroup;
import com.shg.model.SHGMember;
import com.shg.model.Transaction;
import com.shg.factory.FinancialRecordFactory;
import com.shg.observer.BalanceObserver;
import com.shg.observer.BalanceSubject;
import com.shg.repository.SHGGroupRepository;
import com.shg.repository.SHGMemberRepository;
import com.shg.repository.TransactionRepository;
import com.shg.state.TransactionStateManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionService implements BalanceSubject {

    private final TransactionRepository transactionRepository;
    private final SHGGroupRepository shgGroupRepository;
    private final SHGMemberRepository shgMemberRepository;
    private final FinancialRecordFactory financialRecordFactory;
    
    private final List<BalanceObserver> observers = new ArrayList<>();
    
    @Autowired(required = false)
    private TransactionStateManager stateManager;

    public TransactionService(TransactionRepository transactionRepository,
                              SHGGroupRepository shgGroupRepository,
                              SHGMemberRepository shgMemberRepository,
                              FinancialRecordFactory financialRecordFactory) {
        this.transactionRepository = transactionRepository;
        this.shgGroupRepository = shgGroupRepository;
        this.shgMemberRepository = shgMemberRepository;
        this.financialRecordFactory = financialRecordFactory;
    }

    public Transaction createTransaction(Transaction transaction) {
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setType(normalizeType(transaction.getType()));
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDateTime.now());
        }

        Transaction saved = transactionRepository.save(transaction);
        updateAggregates(saved);
        return saved;
    }

    private void updateAggregates(Transaction transaction) {
        SHGGroup group = transaction.getShgGroup();
        if (group != null) {
            double currentBalance = group.getTotalBalance() == null ? 0.0 : group.getTotalBalance();
            switch (transaction.getType()) {
                case "SAVINGS":
                case "REPAYMENT":
                    group.setTotalBalance(currentBalance + transaction.getAmount());
                    break;
                case "EXPENSE":
                case "LOAN":
                    group.setTotalBalance(currentBalance - transaction.getAmount());
                    break;
                default:
                    break;
            }
            group.setUpdatedAt(LocalDateTime.now());
            shgGroupRepository.save(group);
        }

        SHGMember member = transaction.getMember();
        if (member != null) {
            if ("SAVINGS".equals(transaction.getType())) {
                member.setSavingsAmount(member.getSavingsAmount() + transaction.getAmount());
            } else if ("LOAN".equals(transaction.getType())) {
                member.setLoanAmount(member.getLoanAmount() + transaction.getAmount());
            } else if ("REPAYMENT".equals(transaction.getType())) {
                member.setLoanAmount(Math.max(0.0, member.getLoanAmount() - transaction.getAmount()));
            }
            member.setUpdatedAt(LocalDateTime.now());
            shgMemberRepository.save(member);
        }
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public List<Transaction> getTransactionsByShgGroupId(Long shgGroupId) {
        return transactionRepository.findByShgGroupId(shgGroupId);
    }

    public List<Transaction> getTransactionsByType(String type) {
        return transactionRepository.findAll().stream()
                .filter(transaction -> normalizeType(transaction.getType()).equals(normalizeType(type)))
                .collect(Collectors.toList());
    }

    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByTransactionDateBetween(startDate, endDate);
    }

    public List<Transaction> getTransactionsByShgGroupAndDateRange(Long shgGroupId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByShgGroupIdAndTransactionDateBetween(shgGroupId, startDate, endDate);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .collect(Collectors.toList());
    }

    public List<MonthlyReport> generateMonthlySnapshots(Long shgGroupId) {
        List<Transaction> transactions = getTransactionsByShgGroupId(shgGroupId);
        List<YearMonth> months = transactions.stream()
                .map(transaction -> YearMonth.from(transaction.getTransactionDate()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<MonthlyReport> reports = new ArrayList<>();
        for (YearMonth yearMonth : months) {
            List<Transaction> monthlyTransactions = transactions.stream()
                    .filter(transaction -> YearMonth.from(transaction.getTransactionDate()).equals(yearMonth))
                    .collect(Collectors.toList());

            MonthlyReport report = financialRecordFactory.createMonthlyReport(
                    yearMonth.getMonthValue(),
                    yearMonth.getYear(),
                    shgGroupRepository.findById(shgGroupId).orElse(null),
                    sumByType(monthlyTransactions, "SAVINGS"),
                    sumByType(monthlyTransactions, "LOAN"),
                    sumByType(monthlyTransactions, "EXPENSE"),
                    monthlyTransactions.size());
            reports.add(report);
        }
        return reports;
    }

    private double sumByType(List<Transaction> transactions, String type) {
        return transactions.stream()
                .filter(transaction -> type.equals(normalizeType(transaction.getType())))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private String normalizeType(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ENGLISH);
    }

    // ============ BalanceSubject Implementation (Observer Pattern) ============

    @Override
    public void addObserver(BalanceObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println("Observer registered: " + observer.getClass().getSimpleName());
        }
    }

    @Override
    public void removeObserver(BalanceObserver observer) {
        if (observers.remove(observer)) {
            System.out.println("Observer removed: " + observer.getClass().getSimpleName());
        }
    }

    @Override
    public void notifyBalanceChange(Object group, double oldBalance, double newBalance, Object changedBy) {
        if (group instanceof SHGGroup && changedBy instanceof SHGMember) {
            SHGGroup shgGroup = (SHGGroup) group;
            SHGMember member = (SHGMember) changedBy;
            
            for (BalanceObserver observer : observers) {
                try {
                    observer.onBalanceChanged(shgGroup, oldBalance, newBalance, member);
                } catch (Exception e) {
                    System.err.println("Error notifying observer " + observer.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void notifyMemberBalanceChange(Object member, double savingsChange, double loanChange, Object changedBy) {
        if (member instanceof SHGMember && changedBy instanceof SHGMember) {
            SHGMember shgMember = (SHGMember) member;
            SHGMember modifier = (SHGMember) changedBy;
            
            for (BalanceObserver observer : observers) {
                try {
                    observer.onMemberBalanceChanged(shgMember, savingsChange, loanChange, modifier);
                } catch (Exception e) {
                    System.err.println("Error notifying observer " + observer.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void notifyTransactionPending(Long transactionId, double amount, Object member) {
        if (member instanceof SHGMember) {
            SHGMember shgMember = (SHGMember) member;
            
            for (BalanceObserver observer : observers) {
                try {
                    observer.onTransactionPending(transactionId, amount, shgMember);
                } catch (Exception e) {
                    System.err.println("Error notifying observer " + observer.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }

    // ============ Role-Based Transaction Management Methods ============

    /**
     * Delete transaction - only ACCOUNTANT and PRESIDENT can delete
     */
    public void deleteTransaction(Long transactionId) {
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isPresent()) {
            // Reverse the balance changes
            Transaction trans = transaction.get();
            reverseAggregates(trans);
            transactionRepository.deleteById(transactionId);
            System.out.println("Transaction " + transactionId + " deleted successfully");
        }
    }

    /**
     * Reverse balance changes for a transaction
     */
    private void reverseAggregates(Transaction transaction) {
        SHGGroup group = transaction.getShgGroup();
        if (group != null) {
            double currentBalance = group.getTotalBalance() == null ? 0.0 : group.getTotalBalance();
            switch (transaction.getType()) {
                case "SAVINGS":
                case "REPAYMENT":
                    group.setTotalBalance(currentBalance - transaction.getAmount());
                    break;
                case "EXPENSE":
                case "LOAN":
                    group.setTotalBalance(currentBalance + transaction.getAmount());
                    break;
                default:
                    break;
            }
            group.setUpdatedAt(LocalDateTime.now());
            shgGroupRepository.save(group);
        }

        SHGMember member = transaction.getMember();
        if (member != null) {
            if ("SAVINGS".equals(transaction.getType())) {
                member.setSavingsAmount(member.getSavingsAmount() - transaction.getAmount());
            } else if ("LOAN".equals(transaction.getType())) {
                member.setLoanAmount(member.getLoanAmount() - transaction.getAmount());
            } else if ("REPAYMENT".equals(transaction.getType())) {
                member.setLoanAmount(member.getLoanAmount() + transaction.getAmount());
            }
            member.setUpdatedAt(LocalDateTime.now());
            shgMemberRepository.save(member);
        }
    }

    /**
     * Approve a transaction (state transition)
     */
    public void approveTransaction(Long transactionId, SHGMember approver) {
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isPresent()) {
            if (stateManager != null) {
                stateManager.approveTransaction(transaction.get(), approver);
                transactionRepository.save(transaction.get());
            }
        }
    }

    /**
     * Reject a transaction (state transition)
     */
    public void rejectTransaction(Long transactionId, SHGMember rejector, String reason) {
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isPresent()) {
            if (stateManager != null) {
                stateManager.rejectTransaction(transaction.get(), rejector, reason);
                transactionRepository.save(transaction.get());
            }
        }
    }

    /**
     * Apply transaction to balances (used by state handler)
     */
    public void applyTransactionToBalances(Transaction transaction) {
        SHGMember recordedByMember = null;
        // This would need to be set from context - for now we use the first group member
        List<SHGMember> members = shgMemberRepository.findAll();
        if (!members.isEmpty()) {
            recordedByMember = members.get(0);
        }
        
        updateAggregates(transaction, recordedByMember);
    }

    /**
     * Update aggregates with observer notification
     */
    private void updateAggregates(Transaction transaction, SHGMember changedBy) {
        SHGGroup group = transaction.getShgGroup();
        if (group != null && changedBy != null) {
            double oldBalance = group.getTotalBalance() == null ? 0.0 : group.getTotalBalance();
            double newBalance = oldBalance;
            
            switch (transaction.getType()) {
                case "SAVINGS":
                case "REPAYMENT":
                    newBalance = oldBalance + transaction.getAmount();
                    group.setTotalBalance(newBalance);
                    break;
                case "EXPENSE":
                case "LOAN":
                    newBalance = oldBalance - transaction.getAmount();
                    group.setTotalBalance(newBalance);
                    break;
                default:
                    break;
            }
            
            group.setUpdatedAt(LocalDateTime.now());
            shgGroupRepository.save(group);
            
            // Notify observers
            notifyBalanceChange(group, oldBalance, newBalance, changedBy);
        }

        SHGMember member = transaction.getMember();
        if (member != null && changedBy != null) {
            double savingsChange = 0;
            double loanChange = 0;
            
            if ("SAVINGS".equals(transaction.getType())) {
                savingsChange = transaction.getAmount();
                member.setSavingsAmount(member.getSavingsAmount() + transaction.getAmount());
            } else if ("LOAN".equals(transaction.getType())) {
                loanChange = transaction.getAmount();
                member.setLoanAmount(member.getLoanAmount() + transaction.getAmount());
            } else if ("REPAYMENT".equals(transaction.getType())) {
                loanChange = -transaction.getAmount();
                member.setLoanAmount(Math.max(0.0, member.getLoanAmount() - transaction.getAmount()));
            }
            
            member.setUpdatedAt(LocalDateTime.now());
            shgMemberRepository.save(member);
            
            // Notify observers
            notifyMemberBalanceChange(member, savingsChange, loanChange, changedBy);
        }
    }
}
