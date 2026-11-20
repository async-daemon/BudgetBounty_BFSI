package com.budgetbounty.controller;

import com.budgetbounty.dto.investment.*;
import com.budgetbounty.security.UserPrincipal;
import com.budgetbounty.service.InvestmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;

    @GetMapping
    public ResponseEntity<List<InvestmentDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(investmentService.listInvestments(principal.getId()));
    }

    @GetMapping("/goals")
    public ResponseEntity<List<InvestmentGoalDto>> listGoals(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(investmentService.listGoals(principal.getId()));
    }

    @PostMapping("/goals")
    public ResponseEntity<InvestmentGoalDto> createGoal(@AuthenticationPrincipal UserPrincipal principal,
                                                          @Valid @RequestBody CreateGoalRequest request) {
        return ResponseEntity.ok(investmentService.createGoal(principal.getId(), request));
    }

    @PostMapping
    public ResponseEntity<InvestmentDto> create(@AuthenticationPrincipal UserPrincipal principal,
                                                 @Valid @RequestBody CreateInvestmentRequest request) {
        return ResponseEntity.ok(investmentService.createInvestment(principal.getId(), request));
    }
}
