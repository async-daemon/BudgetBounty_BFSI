package com.budgetbounty.controller;

import com.budgetbounty.dto.dashboard.*;
import com.budgetbounty.security.UserPrincipal;
import com.budgetbounty.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewDto> overview(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.overview(principal.getId()));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<LinkedAccountDto>> accounts(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.listLinkedAccounts(principal.getId()));
    }

    @PostMapping("/accounts")
    public ResponseEntity<LinkedAccountDto> linkAccount(@AuthenticationPrincipal UserPrincipal principal,
                                                          @Valid @RequestBody LinkAccountRequest request) {
        return ResponseEntity.ok(dashboardService.linkAccount(principal.getId(), request));
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> unlinkAccount(@AuthenticationPrincipal UserPrincipal principal,
                                               @PathVariable Long id) {
        dashboardService.unlinkAccount(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDto>> notifications(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.listNotifications(principal.getId()));
    }

    @PostMapping("/notifications/mark-read")
    public ResponseEntity<Void> markRead(@AuthenticationPrincipal UserPrincipal principal) {
        dashboardService.markNotificationsRead(principal.getId());
        return ResponseEntity.noContent().build();
    }
}
