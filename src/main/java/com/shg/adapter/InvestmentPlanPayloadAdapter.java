package com.shg.adapter;

import com.shg.model.InvestmentPlan;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class InvestmentPlanPayloadAdapter implements ApiPayloadAdapter<InvestmentPlan> {

    @Override
    public Map<String, Object> adapt(InvestmentPlan plan) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", plan.getId());
        payload.put("name", plan.getPlanName());
        payload.put("provider", plan.getBrokerName());
        payload.put("returnRate", plan.getExpectedReturn());
        payload.put("risk", plan.getRiskLevel());
        payload.put("durationMonths", plan.getDurationMonths());
        payload.put("tenure", plan.getDurationMonths() + " months");
        payload.put("minAmount", plan.getMinimumAmount());
        payload.put("type", plan.getDurationMonths() >= 12 ? "FD" : "SIP");
        payload.put("status", plan.getStatus());
        payload.put("description", plan.getDescription() == null ? "" : plan.getDescription());
        return payload;
    }
}
