package com.shg.strategy;

import org.springframework.stereotype.Component;

/**
 * Simple Interest Strategy
 * Formula: I = P × R × T
 * Where: P = Principal, R = Rate (annual), T = Time (in years)
 */
@Component("simpleInterest")
public class SimpleInterestStrategy implements InterestCalculationStrategy {

    @Override
    public double calculateInterest(double principal, double annualRate, int timeInMonths) {
        double timeInYears = timeInMonths / 12.0;
        double interest = principal * annualRate * timeInYears;
        return Math.round(interest * 100.0) / 100.0; // Round to 2 decimal places
    }

    @Override
    public String getStrategyName() {
        return "Simple Interest";
    }

    @Override
    public String getDescription() {
        return "Interest calculated on principal only: I = P × R × T";
    }
}
