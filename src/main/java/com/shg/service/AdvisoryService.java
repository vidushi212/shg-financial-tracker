package com.shg.service;

import com.shg.model.GovernmentScheme;
import com.shg.model.InvestmentPlan;
import com.shg.model.Recommendation;
import com.shg.repository.GovernmentSchemeRepository;
import com.shg.repository.InvestmentPlanRepository;
import com.shg.repository.RecommendationRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdvisoryService {

    private final InvestmentPlanRepository investmentPlanRepository;
    private final GovernmentSchemeRepository governmentSchemeRepository;
    private final RecommendationRepository recommendationRepository;

    public AdvisoryService(InvestmentPlanRepository investmentPlanRepository,
                           GovernmentSchemeRepository governmentSchemeRepository,
                           RecommendationRepository recommendationRepository) {
        this.investmentPlanRepository = investmentPlanRepository;
        this.governmentSchemeRepository = governmentSchemeRepository;
        this.recommendationRepository = recommendationRepository;
    }

    public List<InvestmentPlan> getApprovedInvestmentPlans() {
        return investmentPlanRepository.findByStatus("APPROVED")
                .stream()
                .sorted(Comparator.comparing(InvestmentPlan::getExpectedReturn).reversed())
                .collect(Collectors.toList());
    }

    public List<GovernmentScheme> getGovernmentSchemes() {
        return governmentSchemeRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(GovernmentScheme::getSchemeName))
                .collect(Collectors.toList());
    }

    public List<Recommendation> getApprovedRecommendations() {
        return recommendationRepository.findByStatus("APPROVED")
                .stream()
                .sorted(Comparator.comparing(Recommendation::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<InvestmentPlan> getInvestmentPlansForRole(String role) {
        if (canManageAdvisory(role)) {
            return investmentPlanRepository.findAll()
                    .stream()
                    .sorted(Comparator.comparing(InvestmentPlan::getUpdatedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        }
        return getApprovedInvestmentPlans();
    }

    public InvestmentPlan createInvestmentPlan(Map<String, Object> payload) {
        InvestmentPlan plan = new InvestmentPlan();
        applyInvestmentPlan(plan, payload);
        if (plan.getStatus() == null || plan.getStatus().isBlank()) {
            plan.setStatus("APPROVED");
        }
        return investmentPlanRepository.save(plan);
    }

    public InvestmentPlan updateInvestmentPlan(Long id, Map<String, Object> payload) {
        InvestmentPlan plan = investmentPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Investment plan not found."));
        applyInvestmentPlan(plan, payload);
        return investmentPlanRepository.save(plan);
    }

    public void deleteInvestmentPlan(Long id) {
        if (!investmentPlanRepository.existsById(id)) {
            throw new IllegalArgumentException("Investment plan not found.");
        }
        investmentPlanRepository.deleteById(id);
    }

    public GovernmentScheme createGovernmentScheme(Map<String, Object> payload) {
        GovernmentScheme scheme = new GovernmentScheme();
        applyGovernmentScheme(scheme, payload);
        return governmentSchemeRepository.save(scheme);
    }

    public GovernmentScheme updateGovernmentScheme(Long id, Map<String, Object> payload) {
        GovernmentScheme scheme = governmentSchemeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Government scheme not found."));
        applyGovernmentScheme(scheme, payload);
        return governmentSchemeRepository.save(scheme);
    }

    public void deleteGovernmentScheme(Long id) {
        if (!governmentSchemeRepository.existsById(id)) {
            throw new IllegalArgumentException("Government scheme not found.");
        }
        governmentSchemeRepository.deleteById(id);
    }

    public boolean canManageAdvisory(String role) {
        String normalizedRole = normalizeRole(role);
        return "government officer".equals(normalizedRole) || "admin".equals(normalizedRole);
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toLowerCase(Locale.ENGLISH);
    }

    private void applyInvestmentPlan(InvestmentPlan plan, Map<String, Object> payload) {
        plan.setPlanName(requireText(payload.get("name"), "Plan name is required."));
        plan.setBrokerName(requireText(payload.get("provider"), "Provider is required."));
        plan.setDescription(optionalText(payload.get("description")));
        plan.setExpectedReturn(requireDouble(payload.get("returnRate"), "Return rate is required."));
        plan.setRiskLevel(requireText(payload.get("risk"), "Risk level is required."));
        plan.setDurationMonths(requireInteger(payload.get("durationMonths"), "Duration is required."));
        plan.setMinimumAmount(requireDouble(payload.get("minAmount"), "Minimum amount is required."));
        String status = optionalText(payload.get("status"));
        plan.setStatus(status.isBlank() ? "APPROVED" : status.toUpperCase(Locale.ENGLISH));
    }

    private void applyGovernmentScheme(GovernmentScheme scheme, Map<String, Object> payload) {
        scheme.setSchemeName(requireText(payload.get("name"), "Scheme name is required."));
        scheme.setGovernmentBody(requireText(payload.get("issuer"), "Government body is required."));
        scheme.setDescription(optionalText(payload.get("description")));
        scheme.setEligibility(optionalText(payload.get("eligibility")));
        scheme.setMaxLoanAmount(requireDouble(payload.get("maxLoanAmount"), "Maximum amount is required."));
        scheme.setInterestRate(requireDouble(payload.get("interestRate"), "Interest rate is required."));
        scheme.setRepaymentPeriodMonths(requireInteger(payload.get("repaymentPeriodMonths"), "Repayment period is required."));
    }

    private String requireText(Object value, String message) {
        String text = optionalText(value);
        if (text.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return text;
    }

    private String optionalText(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private Double requireDouble(Object value, String message) {
        if (value == null || value.toString().trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        try {
            return Double.parseDouble(value.toString().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(message);
        }
    }

    private Integer requireInteger(Object value, String message) {
        if (value == null || value.toString().trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(message);
        }
    }
}
