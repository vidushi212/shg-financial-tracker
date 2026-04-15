package com.shg.adapter;

import com.shg.model.GovernmentScheme;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GovernmentSchemePayloadAdapter implements ApiPayloadAdapter<GovernmentScheme> {

    @Override
    public Map<String, Object> adapt(GovernmentScheme scheme) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", scheme.getId());
        payload.put("name", scheme.getSchemeName());

        String type = "Scheme";
        String lowerName = scheme.getSchemeName() == null ? "" : scheme.getSchemeName().toLowerCase();
        if (lowerName.contains("mudra") || lowerName.contains("loan") || lowerName.contains("nrlm")) {
            type = "Loan";
        } else if (lowerName.contains("pension")) {
            type = "Pension";
        } else if (lowerName.contains("bima") || lowerName.contains("insurance")) {
            type = "Insurance";
        } else if (lowerName.contains("dhan") || lowerName.contains("bank")) {
            type = "Banking";
        } else if (lowerName.contains("shg") || lowerName.contains("self")) {
            type = "SHG";
        }

        payload.put("type", type);
        payload.put("eligibility", scheme.getEligibility() == null ? "" : scheme.getEligibility());
        String issuer = scheme.getGovernmentBody() == null ? "" : scheme.getGovernmentBody();
        payload.put("issuer", issuer);
        if (scheme.getMaxLoanAmount() == null || scheme.getMaxLoanAmount() <= 0) {
            payload.put("benefit", issuer.isEmpty() ? "Scheme benefits available." : "Policy benefits available from " + issuer + ".");
        } else {
            payload.put("benefit", String.format("Up to ₹%.0f at %.2f%% for %d months",
                    scheme.getMaxLoanAmount(),
                    scheme.getInterestRate() == null ? 0 : scheme.getInterestRate(),
                    scheme.getRepaymentPeriodMonths() == null ? 0 : scheme.getRepaymentPeriodMonths()));
        }
        payload.put("description", scheme.getDescription() == null ? "" : scheme.getDescription());
        return payload;
    }
}
