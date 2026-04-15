package com.shg.strategy;

import org.springframework.stereotype.Component;

/**
 * Compound Interest Strategy
 * Formula: A = P(1 + r/n)^(nt) - P
 * Where: P = Principal, r = annual rate, n = compounds per year, t = years
 * Using monthly compounding (n = 12)
 */
@Component("compoundInterest")
public class CompoundInterestStrategy implements InterestCalculationStrategy {

    private static final int COMPOUNDS_PER_YEAR = 12; // Monthly compounding

    @Override
    public double calculateInterest(double principal, double annualRate, int timeInMonths) {
        double rate = annualRate / COMPOUNDS_PER_YEAR;
        double numCompounds = timeInMonths;
        
        // Formula: A = P(1 + r)^n
        double amount = principal * Math.pow(1 + rate, numCompounds);
        
        // Interest = Amount - Principal
        double interest = amount - principal;
        
        return Math.round(interest * 100.0) / 100.0; // Round to 2 decimal places
    }

    @Override
    public String getStrategyName() {
        return "Compound Interest";
    }

    @Override
    public String getDescription() {
        return "Interest compounded monthly on principal + accumulated interest: A = P(1 + r)^n";
    }
}
