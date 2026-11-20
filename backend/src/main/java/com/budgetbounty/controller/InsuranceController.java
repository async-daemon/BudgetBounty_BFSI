package com.budgetbounty.controller;

import com.budgetbounty.dto.insurance.CreatePolicyRequest;
import com.budgetbounty.dto.insurance.InsurancePolicyDto;
import com.budgetbounty.security.UserPrincipal;
import com.budgetbounty.service.InsuranceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insurance")
@RequiredArgsConstructor
public class InsuranceController {

    private final InsuranceService insuranceService;

    @GetMapping
    public ResponseEntity<List<InsurancePolicyDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(insuranceService.listPolicies(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<InsurancePolicyDto> create(@AuthenticationPrincipal UserPrincipal principal,
                                                       @Valid @RequestBody CreatePolicyRequest request) {
        return ResponseEntity.ok(insuranceService.createPolicy(principal.getId(), request));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<InsurancePolicyDto> pay(@AuthenticationPrincipal UserPrincipal principal,
                                                   @PathVariable Long id) {
        return ResponseEntity.ok(insuranceService.payPremium(principal.getId(), id));
    }
}
