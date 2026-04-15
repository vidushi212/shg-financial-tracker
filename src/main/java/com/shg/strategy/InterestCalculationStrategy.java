package com.shg.strategy;

/**
 * Strategy interface for calculating interest
 * Different calculation strategies for different loan/savings schemes
 */
public interface InterestCalculationStrategy {
    
    /**
     * Calculate interest on given principal amount
     * @param principal The principal amount
     * @param annualRate Annual interest rate (as decimal, e.g., 0.05 for 5%)
     * @param timeInMonths Time period in months
     * @return Calculated interest amount
     */
    double calculateInterest(double principal, double annualRate, int timeInMonths);

    /**
     * Get strategy name for display
     */
    String getStrategyName();

    /**
     * Get strategy description
     */
    String getDescription();
}
