package com.shg.controller;

import com.shg.facade.AdvisoryFacade;
import com.shg.adapter.GovernmentSchemePayloadAdapter;
import com.shg.adapter.InvestmentPlanPayloadAdapter;
import com.shg.model.GovernmentScheme;
import com.shg.model.InvestmentPlan;
import com.shg.service.AdvisoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/advisory")
@CrossOrigin(origins = "*")
public class AdvisoryApiController {

    private final AdvisoryFacade advisoryFacade;
    private final AdvisoryService advisoryService;
    private final InvestmentPlanPayloadAdapter investmentPlanPayloadAdapter;
    private final GovernmentSchemePayloadAdapter governmentSchemePayloadAdapter;

    public AdvisoryApiController(AdvisoryFacade advisoryFacade,
                                 AdvisoryService advisoryService,
                                 InvestmentPlanPayloadAdapter investmentPlanPayloadAdapter,
                                 GovernmentSchemePayloadAdapter governmentSchemePayloadAdapter) {
        this.advisoryFacade = advisoryFacade;
        this.advisoryService = advisoryService;
        this.investmentPlanPayloadAdapter = investmentPlanPayloadAdapter;
        this.governmentSchemePayloadAdapter = governmentSchemePayloadAdapter;
    }

    @GetMapping("/investment-plans")
    public ResponseEntity<List<Map<String, Object>>> getInvestmentPlans(
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (advisoryService.canManageAdvisory(userRole)) {
            List<Map<String, Object>> payload = advisoryService.getInvestmentPlansForRole(userRole).stream()
                    .map(investmentPlanPayloadAdapter::adapt)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(payload);
        }
        return ResponseEntity.ok(advisoryFacade.getInvestmentPlans());
    }

    @GetMapping("/govt-schemes")
    public ResponseEntity<List<Map<String, Object>>> getSchemes() {
        return ResponseEntity.ok(advisoryFacade.getGovernmentSchemes());
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<Map<String, Object>>> getRecommendations() {
        return ResponseEntity.ok(advisoryFacade.getRecommendations());
    }

    @PostMapping("/investment-plans")
    public ResponseEntity<?> createInvestmentPlan(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestBody Map<String, Object> payload) {
        if (!advisoryService.canManageAdvisory(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only government officers and admins can manage investment plans."));
        }
        try {
            InvestmentPlan created = advisoryService.createInvestmentPlan(payload);
            return ResponseEntity.status(HttpStatus.CREATED).body(investmentPlanPayloadAdapter.adapt(created));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/investment-plans/{id}")
    public ResponseEntity<?> updateInvestmentPlan(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        if (!advisoryService.canManageAdvisory(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only government officers and admins can manage investment plans."));
        }
        try {
            InvestmentPlan updated = advisoryService.updateInvestmentPlan(id, payload);
            return ResponseEntity.ok(investmentPlanPayloadAdapter.adapt(updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/investment-plans/{id}")
    public ResponseEntity<?> deleteInvestmentPlan(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable Long id) {
        if (!advisoryService.canManageAdvisory(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only government officers and admins can manage investment plans."));
        }
        try {
            advisoryService.deleteInvestmentPlan(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Investment plan deleted."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/govt-schemes")
    public ResponseEntity<?> createGovernmentScheme(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestBody Map<String, Object> payload) {
        if (!advisoryService.canManageAdvisory(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only government officers and admins can manage government schemes."));
        }
        try {
            GovernmentScheme created = advisoryService.createGovernmentScheme(payload);
            return ResponseEntity.status(HttpStatus.CREATED).body(governmentSchemePayloadAdapter.adapt(created));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/govt-schemes/{id}")
    public ResponseEntity<?> updateGovernmentScheme(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        if (!advisoryService.canManageAdvisory(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only government officers and admins can manage government schemes."));
        }
        try {
            GovernmentScheme updated = advisoryService.updateGovernmentScheme(id, payload);
            return ResponseEntity.ok(governmentSchemePayloadAdapter.adapt(updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/govt-schemes/{id}")
    public ResponseEntity<?> deleteGovernmentScheme(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable Long id) {
        if (!advisoryService.canManageAdvisory(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only government officers and admins can manage government schemes."));
        }
        try {
            advisoryService.deleteGovernmentScheme(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Government scheme deleted."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}
