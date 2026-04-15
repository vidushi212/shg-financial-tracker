package com.shg.service;

import com.shg.model.SHGGroup;
import com.shg.model.SHGMember;
import com.shg.model.Transaction;
import com.shg.repository.SHGGroupRepository;
import com.shg.repository.SHGMemberRepository;
import com.shg.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final SHGGroupRepository groupRepository;
    private final SHGMemberRepository memberRepository;
    private final TransactionRepository transactionRepository;

    public DashboardService(SHGGroupRepository groupRepository,
                            SHGMemberRepository memberRepository,
                            TransactionRepository transactionRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.transactionRepository = transactionRepository;
    }

    public Map<String, Object> getDashboardStats() {
        SHGGroup group = groupRepository.findAll()
                .stream()
                .min(Comparator.comparing(SHGGroup::getId))
                .orElse(null);

        List<Transaction> transactions = transactionRepository.findAll();
        double totalSavings = memberRepository.findAll().stream()
                .mapToDouble(member -> member.getSavingsAmount() == null ? 0.0 : member.getSavingsAmount())
                .sum();
        double totalLoans = memberRepository.findAll().stream()
                .mapToDouble(member -> member.getLoanAmount() == null ? 0.0 : member.getLoanAmount())
                .sum();

        return Map.of(
                "groupName", group != null ? group.getName() : "My SHG Group",
                "groupSince", group != null ? group.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM yyyy")) : "-",
                "memberCount", memberRepository.count(),
                "totalBalance", group != null ? group.getTotalBalance() : 0.0,
                "totalSavings", totalSavings,
                "totalLoans", totalLoans);
    }

    public List<Map<String, Object>> getDashboardMembers() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        List<SHGMember> members = memberRepository.findAll();
        return members.stream()
                .sorted(Comparator.comparing(SHGMember::getFullName))
                .map(member -> Map.<String, Object>ofEntries(
                        Map.entry("id", member.getId()),
                        Map.entry("username", Objects.toString(member.getUsername(), "")),
                        Map.entry("name", Objects.toString(member.getFullName(), "")),
                        Map.entry("role", Objects.toString(member.getRole(), "")),
                        Map.entry("email", Objects.toString(member.getEmail(), "")),
                        Map.entry("phoneNumber", Objects.toString(member.getPhoneNumber(), "")),
                        Map.entry("status", Objects.toString(member.getStatus(), "ACTIVE")),
                        Map.entry("joinedDate", member.getJoinedDate() == null ? "" : member.getJoinedDate().format(formatter)),
                        Map.entry("savings", member.getSavingsAmount()),
                        Map.entry("loans", member.getLoanAmount()),
                        Map.entry("active", "ACTIVE".equalsIgnoreCase(member.getStatus()) || "APPROVED".equalsIgnoreCase(member.getStatus()))))
                .collect(Collectors.toList());
    }
}
