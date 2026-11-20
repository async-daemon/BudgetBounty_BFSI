package com.budgetbounty.service;

import com.budgetbounty.dto.rewards.RedemptionDto;
import com.budgetbounty.dto.rewards.RewardCatalogItemDto;
import com.budgetbounty.dto.rewards.RewardTransactionDto;
import com.budgetbounty.entity.Redemption;
import com.budgetbounty.entity.RewardCatalogItem;
import com.budgetbounty.entity.User;
import com.budgetbounty.exception.NotFoundException;
import com.budgetbounty.repository.RedemptionRepository;
import com.budgetbounty.repository.RewardCatalogItemRepository;
import com.budgetbounty.repository.RewardTransactionRepository;
import com.budgetbounty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardCatalogService {

    private final RewardCatalogItemRepository catalogItemRepository;
    private final RedemptionRepository redemptionRepository;
    private final RewardTransactionRepository rewardTransactionRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;

    public List<RewardCatalogItemDto> listCatalog() {
        return catalogItemRepository.findByActiveTrue().stream().map(RewardCatalogItemDto::from).toList();
    }

    public List<RewardTransactionDto> ledger(Long userId) {
        return rewardTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(RewardTransactionDto::from).toList();
    }

    public List<RedemptionDto> redemptionHistory(Long userId) {
        return redemptionRepository.findByUserIdOrderByRedeemedAtDesc(userId).stream()
                .map(RedemptionDto::from).toList();
    }

    @Transactional
    public RedemptionDto redeem(Long userId, Long catalogItemId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        RewardCatalogItem item = catalogItemRepository.findById(catalogItemId)
                .orElseThrow(() -> new NotFoundException("Catalog item not found"));

        rewardService.redeemPoints(user, item.getPointsRequired(), "Redeemed: " + item.getName());

        Redemption redemption = Redemption.builder()
                .user(user)
                .item(item)
                .pointsSpent(item.getPointsRequired())
                .status("completed")
                .build();
        redemptionRepository.save(redemption);

        return RedemptionDto.from(redemption);
    }
}
