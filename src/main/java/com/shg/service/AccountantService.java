package com.shg.service;

import com.shg.factory.FinancialRecordFactory;
import com.shg.model.SHGGroup;
import com.shg.model.SHGMember;
import com.shg.model.Transaction;
import com.shg.repository.SHGGroupRepository;
import com.shg.repository.SHGMemberRepository;
import com.shg.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountantService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final SHGGroupRepository groupRepository;
    private final SHGMemberRepository memberRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final FinancialRecordFactory financialRecordFactory;

    public AccountantService(SHGGroupRepository groupRepository,
                             SHGMemberRepository memberRepository,
                             TransactionRepository transactionRepository,
                             TransactionService transactionService,
                             FinancialRecordFactory financialRecordFactory) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
        this.financialRecordFactory = financialRecordFactory;
    }

    public Map<String, Object> getOverview() {
        SHGGroup group = getPrimaryGroup();
        List<SHGMember> members = memberRepository.findAll();
        List<Transaction> transactions = sortTransactions(transactionRepository.findAll());
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();

        double outstandingLoans = members.stream()
                .mapToDouble(member -> safe(member.getLoanAmount()))
                .sum();
        double totalSavings = members.stream()
                .mapToDouble(member -> safe(member.getSavingsAmount()))
                .sum();
        long membersWithLoans = members.stream()
                .filter(member -> safe(member.getLoanAmount()) > 0)
                .count();
        long overdueMembers = members.stream()
                .filter(this::isOverdue)
                .count();
        long highRiskMembers = members.stream()
                .filter(member -> "High".equalsIgnoreCase(accountHealth(member)))
                .count();
        long pendingTransactions = transactions.stream()
                .filter(transaction -> "PENDING".equalsIgnoreCase(transaction.getState()))
                .count();
        double collectionsThisMonth = transactions.stream()
                .filter(transaction -> "REPAYMENT".equalsIgnoreCase(transaction.getType()))
                .filter(transaction -> !transaction.getTransactionDate().isBefore(monthStart))
                .mapToDouble(Transaction::getAmount)
                .sum();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("groupName", group != null ? group.getName() : "My SHG Group");
        response.put("groupBalance", group != null ? safe(group.getTotalBalance()) : 0.0);
        response.put("totalSavings", totalSavings);
        response.put("outstandingLoans", outstandingLoans);
        response.put("membersWithLoans", membersWithLoans);
        response.put("overdueMembers", overdueMembers);
        response.put("highRiskMembers", highRiskMembers);
        response.put("pendingTransactions", pendingTransactions);
        response.put("collectionsThisMonth", collectionsThisMonth);
        response.put("portfolioHealth", portfolioHealth(overdueMembers, membersWithLoans));
        response.put("recentTransactions", transactions.stream()
                .limit(6)
                .map(this::toTransactionSummary)
                .collect(Collectors.toList()));
        response.put("alerts", buildAlerts(members, transactions));
        return response;
    }

    public List<Map<String, Object>> getMemberAccountSummaries() {
        List<Transaction> transactions = sortTransactions(transactionRepository.findAll());
        return memberRepository.findAll().stream()
                .sorted(Comparator.comparing(SHGMember::getFullName, String.CASE_INSENSITIVE_ORDER))
                .map(member -> toMemberAccountSummary(member, transactions))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getReportingSnapshot() {
        List<SHGMember> members = memberRepository.findAll();
        List<Transaction> transactions = sortTransactions(transactionRepository.findAll());

        List<Map<String, Object>> monthly = buildMonthlyTrend(transactions);
        List<Map<String, Object>> topBorrowers = members.stream()
                .filter(member -> safe(member.getLoanAmount()) > 0)
                .sorted(Comparator.comparingDouble((SHGMember member) -> safe(member.getLoanAmount())).reversed())
                .limit(5)
                .map(member -> Map.<String, Object>of(
                        "name", member.getFullName(),
                        "loanAmount", safe(member.getLoanAmount()),
                        "savingsAmount", safe(member.getSavingsAmount()),
                        "health", accountHealth(member)))
                .collect(Collectors.toList());

        List<Map<String, Object>> topSavers = members.stream()
                .sorted(Comparator.comparingDouble((SHGMember member) -> safe(member.getSavingsAmount())).reversed())
                .limit(5)
                .map(member -> Map.<String, Object>of(
                        "name", member.getFullName(),
                        "savingsAmount", safe(member.getSavingsAmount()),
                        "loanAmount", safe(member.getLoanAmount())))
                .collect(Collectors.toList());

        Map<String, Object> loanPortfolio = new LinkedHashMap<>();
        loanPortfolio.put("totalOutstanding", members.stream().mapToDouble(member -> safe(member.getLoanAmount())).sum());
        loanPortfolio.put("membersWithLoans", members.stream().filter(member -> safe(member.getLoanAmount()) > 0).count());
        loanPortfolio.put("overdueMembers", members.stream().filter(this::isOverdue).count());
        loanPortfolio.put("collectionsLast30Days", transactions.stream()
                .filter(transaction -> "REPAYMENT".equalsIgnoreCase(transaction.getType()))
                .filter(transaction -> !transaction.getTransactionDate().isBefore(LocalDateTime.now().minusDays(30)))
                .mapToDouble(Transaction::getAmount)
                .sum());

        return Map.of(
                "monthly", monthly,
                "topBorrowers", topBorrowers,
                "topSavers", topSavers,
                "loanPortfolio", loanPortfolio);
    }

    public Map<String, Object> disburseLoan(Map<String, String> payload) {
        SHGMember member = getMember(payload.get("memberId"));
        SHGGroup group = member.getShgGroup() != null ? member.getShgGroup() : getPrimaryGroup();

        double amount = parseAmount(payload.get("amount"));
        String description = payload.getOrDefault("description", "Loan issued by accountant");
        String recordedBy = payload.getOrDefault("recordedBy", "Accountant");
        LocalDateTime date = parseDate(payload.get("date"));

        Transaction transaction = financialRecordFactory.createTransaction(
                "LOAN",
                amount,
                description,
                recordedBy,
                date,
                group,
                member);
        transaction.setState("APPROVED");

        Transaction saved = transactionService.createTransaction(transaction);
        return Map.of(
                "message", "Loan issued successfully",
                "transaction", toTransactionSummary(saved),
                "memberLoanBalance", safe(memberRepository.findById(member.getId()).orElse(member).getLoanAmount()));
    }

    public Map<String, Object> recordRepayment(Map<String, String> payload) {
        SHGMember member = getMember(payload.get("memberId"));
        SHGGroup group = member.getShgGroup() != null ? member.getShgGroup() : getPrimaryGroup();

        double amount = parseAmount(payload.get("amount"));
        String description = payload.getOrDefault("description", "Loan repayment received");
        String recordedBy = payload.getOrDefault("recordedBy", "Accountant");
        LocalDateTime date = parseDate(payload.get("date"));

        Transaction transaction = financialRecordFactory.createTransaction(
                "REPAYMENT",
                amount,
                description,
                recordedBy,
                date,
                group,
                member);
        transaction.setState("APPROVED");

        Transaction saved = transactionService.createTransaction(transaction);
        return Map.of(
                "message", "Repayment recorded successfully",
                "transaction", toTransactionSummary(saved),
                "memberLoanBalance", safe(memberRepository.findById(member.getId()).orElse(member).getLoanAmount()));
    }

    private List<Map<String, Object>> buildMonthlyTrend(List<Transaction> transactions) {
        Map<YearMonth, List<Transaction>> grouped = transactions.stream()
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.getTransactionDate())));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    YearMonth month = entry.getKey();
                    List<Transaction> monthTransactions = entry.getValue();
                    return Map.<String, Object>of(
                            "month", month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + month.getYear(),
                            "savings", sumByType(monthTransactions, "SAVINGS"),
                            "loans", sumByType(monthTransactions, "LOAN"),
                            "repayments", sumByType(monthTransactions, "REPAYMENT"),
                            "expenses", sumByType(monthTransactions, "EXPENSE"));
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildAlerts(List<SHGMember> members, List<Transaction> transactions) {
        List<Map<String, Object>> alerts = new ArrayList<>();

        members.stream()
                .filter(this::isOverdue)
                .sorted(Comparator.comparingDouble((SHGMember member) -> safe(member.getLoanAmount())).reversed())
                .limit(3)
                .forEach(member -> alerts.add(Map.of(
                        "title", member.getFullName() + " needs follow-up",
                        "detail", "Outstanding loan of Rs. " + Math.round(safe(member.getLoanAmount())) + " with no repayment in the last 30 days.",
                        "severity", "high")));

        long pendingCount = transactions.stream().filter(transaction -> "PENDING".equalsIgnoreCase(transaction.getState())).count();
        if (pendingCount > 0) {
            alerts.add(Map.of(
                    "title", "Pending transactions awaiting review",
                    "detail", pendingCount + " transaction(s) still need approval or rejection.",
                    "severity", "medium"));
        }

        double expensesThisMonth = transactions.stream()
                .filter(transaction -> "EXPENSE".equalsIgnoreCase(transaction.getType()))
                .filter(transaction -> !transaction.getTransactionDate().isBefore(YearMonth.now().atDay(1).atStartOfDay()))
                .mapToDouble(Transaction::getAmount)
                .sum();
        double savingsThisMonth = transactions.stream()
                .filter(transaction -> "SAVINGS".equalsIgnoreCase(transaction.getType()))
                .filter(transaction -> !transaction.getTransactionDate().isBefore(YearMonth.now().atDay(1).atStartOfDay()))
                .mapToDouble(Transaction::getAmount)
                .sum();
        if (expensesThisMonth > savingsThisMonth && expensesThisMonth > 0) {
            alerts.add(Map.of(
                    "title", "Monthly expenses are ahead of savings",
                    "detail", "Current month expenses are higher than savings collections. Review spend approvals.",
                    "severity", "medium"));
        }

        return alerts;
    }

    private Map<String, Object> toMemberAccountSummary(SHGMember member, List<Transaction> transactions) {
        List<Transaction> memberTransactions = transactions.stream()
                .filter(transaction -> transaction.getMember() != null && member.getId().equals(transaction.getMember().getId()))
                .collect(Collectors.toList());

        Optional<Transaction> lastTransaction = memberTransactions.stream().findFirst();
        Optional<Transaction> lastSavings = memberTransactions.stream()
                .filter(transaction -> "SAVINGS".equalsIgnoreCase(transaction.getType()))
                .findFirst();
        Optional<Transaction> lastRepayment = memberTransactions.stream()
                .filter(transaction -> "REPAYMENT".equalsIgnoreCase(transaction.getType()))
                .findFirst();

        double savings = safe(member.getSavingsAmount());
        double loan = safe(member.getLoanAmount());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", member.getId());
        payload.put("name", member.getFullName());
        payload.put("role", member.getRole());
        payload.put("status", member.getStatus());
        payload.put("savingsAmount", savings);
        payload.put("loanAmount", loan);
        payload.put("netPosition", savings - loan);
        payload.put("health", accountHealth(member));
        payload.put("loanStatus", loanStatus(member));
        payload.put("transactionCount", memberTransactions.size());
        payload.put("lastTransactionDate", lastTransaction.map(tx -> formatDate(tx.getTransactionDate())).orElse("-"));
        payload.put("lastSavingsDate", lastSavings.map(tx -> formatDate(tx.getTransactionDate())).orElse("-"));
        payload.put("lastRepaymentDate", lastRepayment.map(tx -> formatDate(tx.getTransactionDate())).orElse("-"));
        payload.put("requiresAttention", isOverdue(member) || "High".equalsIgnoreCase(accountHealth(member)));
        return payload;
    }

    private Map<String, Object> toTransactionSummary(Transaction transaction) {
        return Map.of(
                "id", transaction.getId(),
                "type", titleCase(transaction.getType()),
                "amount", transaction.getAmount(),
                "member", transaction.getMember() != null ? transaction.getMember().getFullName() : transaction.getRecordedBy(),
                "date", formatDate(transaction.getTransactionDate()),
                "state", titleCase(transaction.getState() == null ? "APPROVED" : transaction.getState()),
                "description", transaction.getDescription() == null ? "" : transaction.getDescription());
    }

    private String portfolioHealth(long overdueMembers, long membersWithLoans) {
        if (membersWithLoans == 0) {
            return "Stable";
        }
        double ratio = (double) overdueMembers / membersWithLoans;
        if (ratio >= 0.5) {
            return "Critical";
        }
        if (ratio >= 0.25) {
            return "Watchlist";
        }
        return "Stable";
    }

    private String accountHealth(SHGMember member) {
        double savings = safe(member.getSavingsAmount());
        double loans = safe(member.getLoanAmount());
        if (loans <= 0) {
            return savings > 0 ? "Healthy" : "Normal";
        }
        double ratio = savings / loans;
        if (isOverdue(member) || ratio < 0.25) {
            return "High";
        }
        if (ratio < 0.6) {
            return "Medium";
        }
        return "Healthy";
    }

    private String loanStatus(SHGMember member) {
        double loan = safe(member.getLoanAmount());
        if (loan <= 0) {
            return "No Active Loan";
        }
        if (isOverdue(member)) {
            return "Overdue";
        }
        if (loan > Math.max(5000.0, safe(member.getSavingsAmount()) * 1.5)) {
            return "Monitoring";
        }
        return "On Track";
    }

    private boolean isOverdue(SHGMember member) {
        if (safe(member.getLoanAmount()) <= 0) {
            return false;
        }
        LocalDateTime lastRepayment = transactionRepository.findAll().stream()
                .filter(transaction -> transaction.getMember() != null && member.getId().equals(transaction.getMember().getId()))
                .filter(transaction -> "REPAYMENT".equalsIgnoreCase(transaction.getType()))
                .map(Transaction::getTransactionDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime lastLoan = transactionRepository.findAll().stream()
                .filter(transaction -> transaction.getMember() != null && member.getId().equals(transaction.getMember().getId()))
                .filter(transaction -> "LOAN".equalsIgnoreCase(transaction.getType()))
                .map(Transaction::getTransactionDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (lastLoan == null) {
            return false;
        }
        if (lastRepayment == null) {
            return lastLoan.isBefore(LocalDateTime.now().minusDays(30));
        }
        return lastRepayment.isBefore(LocalDateTime.now().minusDays(30));
    }

    private SHGMember getMember(String memberIdValue) {
        if (memberIdValue == null || memberIdValue.isBlank()) {
            throw new IllegalArgumentException("Member is required.");
        }
        Long memberId = Long.parseLong(memberIdValue);
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
    }

    private SHGGroup getPrimaryGroup() {
        return groupRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No SHG group available"));
    }

    private List<Transaction> sortTransactions(List<Transaction> transactions) {
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .collect(Collectors.toList());
    }

    private double sumByType(List<Transaction> transactions, String type) {
        return transactions.stream()
                .filter(transaction -> type.equalsIgnoreCase(transaction.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private double parseAmount(String value) {
        try {
            double amount = Double.parseDouble(value);
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount should be greater than zero.");
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid amount.");
        }
    }

    private LocalDateTime parseDate(String value) {
        return (value == null || value.isBlank()) ? LocalDateTime.now() : LocalDate.parse(value).atStartOfDay();
    }

    private double safe(Double value) {
        return value == null ? 0.0 : value;
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_FORMATTER);
    }

    private String titleCase(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.toLowerCase(Locale.ENGLISH).replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
