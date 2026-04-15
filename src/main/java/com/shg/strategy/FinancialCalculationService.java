package com.shg.strategy;

import com.shg.model.SHGMember;
import com.shg.security.MemberRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Financial Calculation Service - Context for Strategy Pattern
 * Manages different interest calculation strategies
 * Role-based access to different calculation types
 */
@Service
public class FinancialCalculationService {

    @Autowired(required = false)
    private Map<String, InterestCalculationStrategy> strategies;
    
    private InterestCalculationStrategy currentStrategy;

    public FinancialCalculationService() {
        this.strategies = new HashMap<>();
    }

    /**
     * Set the strategy to use
     */
    public void setStrategy(String strategyName) {
        InterestCalculationStrategy strategy = strategies.get(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown strategy: " + strategyName + 
                                              ". Available: " + strategies.keySet());
        }
        this.currentStrategy = strategy;
    }

    /**
     * Calculate interest with current strategy
     */
    public double calculateInterest(double principal, double annualRate, int timeInMonths) {
        if (currentStrategy == null) {
            throw new IllegalStateException("Strategy not set. Call setStrategy() first.");
        }
        return currentStrategy.calculateInterest(principal, annualRate, timeInMonths);
    }

    /**
     * Calculate interest with permission check
     */
    public double calculateInterest(SHGMember member, double principal, double annualRate, int timeInMonths) {
        MemberRole role = MemberRole.fromString(member.getRole());
        
        // Only ACCOUNTANT, TREASURER, PRESIDENT, SECRETARY can calculate interest
        if (role != MemberRole.PRESIDENT && role != MemberRole.ACCOUNTANT && 
            role != MemberRole.TREASURER && role != MemberRole.SECRETARY) {
            throw new SecurityException("User " + member.getFullName() + " (" + role.getDisplayName() + 
                                       ") does not have permission to calculate interest");
        }

        if (currentStrategy == null) {
            throw new IllegalStateException("Strategy not set");
        }

        double result = currentStrategy.calculateInterest(principal, annualRate, timeInMonths);
        System.out.println("Interest calculated by " + member.getFullName() + " using " + 
                          currentStrategy.getStrategyName() + ": " + result);
        return result;
    }

    /**
     * Get available strategies - PRESIDENT sees all, others see limited options
     */
    public List<String> getAvailableStrategies(SHGMember member) {
        MemberRole role = MemberRole.fromString(member.getRole());
        
        if (role == MemberRole.PRESIDENT || role == MemberRole.ACCOUNTANT) {
            // Financial roles see all strategies
            return strategies.keySet().stream().collect(Collectors.toList());
        } else {
            // Others see only simple interest
            return List.of("simpleInterest");
        }
    }

    /**
     * Compare different calculation strategies
     */
    public Map<String, Double> compareStrategies(double principal, double annualRate, int timeInMonths) {
        Map<String, Double> results = new HashMap<>();
        
        for (String strategyName : strategies.keySet()) {
            setStrategy(strategyName);
            double interest = calculateInterest(principal, annualRate, timeInMonths);
            results.put(strategyName, interest);
        }
        
        return results;
    }

    /**
     * Get strategy information
     */
    public String getStrategyInfo(String strategyName) {
        InterestCalculationStrategy strategy = strategies.get(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown strategy: " + strategyName);
        }
        return strategy.getStrategyName() + " - " + strategy.getDescription();
    }

    /**
     * Register a new strategy
     */
    public void registerStrategy(String name, InterestCalculationStrategy strategy) {
        strategies.put(name, strategy);
    }
}
