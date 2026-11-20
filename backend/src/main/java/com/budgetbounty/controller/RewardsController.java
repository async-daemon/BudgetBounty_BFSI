package com.budgetbounty.controller;

import com.budgetbounty.dto.rewards.*;
import com.budgetbounty.security.UserPrincipal;
import com.budgetbounty.service.RewardCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardsController {

    private final RewardCatalogService rewardCatalogService;

    @GetMapping("/catalog")
    public ResponseEntity<List<RewardCatalogItemDto>> catalog() {
        return ResponseEntity.ok(rewardCatalogService.listCatalog());
    }

    @GetMapping("/ledger")
    public ResponseEntity<List<RewardTransactionDto>> ledger(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(rewardCatalogService.ledger(principal.getId()));
    }

    @GetMapping("/redemptions")
    public ResponseEntity<List<RedemptionDto>> redemptions(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(rewardCatalogService.redemptionHistory(principal.getId()));
    }

    @PostMapping("/catalog/{itemId}/redeem")
    public ResponseEntity<RedemptionDto> redeem(@AuthenticationPrincipal UserPrincipal principal,
                                                 @PathVariable Long itemId) {
        return ResponseEntity.ok(rewardCatalogService.redeem(principal.getId(), itemId));
    }
}
