package com.budgetbounty.controller;

import com.budgetbounty.dto.budget.BudgetDto;
import com.budgetbounty.dto.budget.CreateBudgetRequest;
import com.budgetbounty.dto.budget.LogExpenseRequest;
import com.budgetbounty.security.UserPrincipal;
import com.budgetbounty.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(budgetService.listForCurrentPeriod(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<BudgetDto> create(@AuthenticationPrincipal UserPrincipal principal,
                                             @Valid @RequestBody CreateBudgetRequest request) {
        return ResponseEntity.ok(budgetService.create(principal.getId(), request));
    }

    @PostMapping("/{id}/log-expense")
    public ResponseEntity<BudgetDto> logExpense(@AuthenticationPrincipal UserPrincipal principal,
                                                 @PathVariable Long id,
                                                 @Valid @RequestBody LogExpenseRequest request) {
        return ResponseEntity.ok(budgetService.logExpense(principal.getId(), id, request));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<BudgetDto> close(@AuthenticationPrincipal UserPrincipal principal,
                                            @PathVariable Long id) {
        return ResponseEntity.ok(budgetService.closePeriod(principal.getId(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        budgetService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
