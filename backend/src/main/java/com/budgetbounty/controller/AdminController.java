package com.budgetbounty.controller;

import com.budgetbounty.dto.admin.*;
import com.budgetbounty.dto.property.PropertyPaymentDto;
import com.budgetbounty.dto.transfer.TransactionDto;
import com.budgetbounty.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @GetMapping("/reward-rules")
    public ResponseEntity<List<AdminRewardRuleDto>> listRewardRules() {
        return ResponseEntity.ok(adminService.listRewardRules());
    }

    @PutMapping("/reward-rules/{module}")
    public ResponseEntity<AdminRewardRuleDto> updateRewardRule(@PathVariable String module,
                                                                @Valid @RequestBody UpdateRewardRuleRequest request) {
        return ResponseEntity.ok(adminService.updateRewardRule(module, request));
    }

    @GetMapping("/catalog")
    public ResponseEntity<List<CatalogItemAdminDto>> listCatalog() {
        return ResponseEntity.ok(adminService.listAllCatalogItems());
    }

    @PostMapping("/catalog")
    public ResponseEntity<CatalogItemAdminDto> createCatalogItem(@Valid @RequestBody CreateCatalogItemRequest request) {
        return ResponseEntity.ok(adminService.createCatalogItem(request));
    }

    @PutMapping("/catalog/{id}/toggle")
    public ResponseEntity<Void> toggleCatalogItem(@PathVariable Long id, @RequestParam boolean active) {
        adminService.toggleCatalogItem(id, active);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> listTransactions() {
        return ResponseEntity.ok(adminService.listAllTransactions());
    }

    @GetMapping("/property-approvals")
    public ResponseEntity<List<PropertyPaymentDto>> pendingApprovals() {
        return ResponseEntity.ok(adminService.listPendingPropertyApprovals());
    }

    @PostMapping("/property-approvals/{id}/approve")
    public ResponseEntity<PropertyPaymentDto> approve(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approvePropertyPayment(id));
    }

    @PostMapping("/property-approvals/{id}/reject")
    public ResponseEntity<PropertyPaymentDto> reject(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.rejectPropertyPayment(id));
    }
}
