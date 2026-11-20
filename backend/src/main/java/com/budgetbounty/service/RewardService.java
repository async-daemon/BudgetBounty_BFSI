package com.budgetbounty.service;

import com.budgetbounty.config.RewardProperties;
import com.budgetbounty.entity.AdminRewardRule;
import com.budgetbounty.entity.Notification;
import com.budgetbounty.entity.RewardTransaction;
import com.budgetbounty.entity.User;
import com.budgetbounty.exception.BadRequestException;
import com.budgetbounty.repository.AdminRewardRuleRepository;
import com.budgetbounty.repository.NotificationRepository;
import com.budgetbounty.repository.RewardTransactionRepository;
import com.budgetbounty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Central reward-points engine. The single place every module credits or
 * debits points, keeping the ledger (RewardTransaction) and
 * User.rewardPoints consistent, and raising an in-app notification for
 * every award. Mirrors the Flask build's app/utils/rewards.py.
 */
@Service
@RequiredArgsConstructor
public class RewardService {

    private final UserRepository userRepository;
    private final RewardTransactionRepository rewardTransactionRepository;
    private final NotificationRepository notificationRepository;
    private final AdminRewardRuleRepository adminRewardRuleRepository;
    private final RewardProperties rewardProperties;

    @Transactional
    public void awardPoints(User user, int points, String sourceModule, String description) {
        awardPoints(user, points, sourceModule, description, true);
    }

    @Transactional
    public void awardPoints(User user, int points, String sourceModule, String description, boolean notify) {
        if (points <= 0) {
            return;
        }
        user.setRewardPoints(user.getRewardPoints() + points);
        userRepository.save(user);

        rewardTransactionRepository.save(RewardTransaction.builder()
                .user(user)
                .points(points)
                .entryType("earn")
                .sourceModule(sourceModule)
                .description(description)
                .build());

        if (notify) {
            notifyUser(user, "Points earned!", "You earned " + points + " points: " + description,
                    "success", "fa-star");
        }
    }

    @Transactional
    public void redeemPoints(User user, int points, String description) {
        if (points <= 0) {
            throw new BadRequestException("Redemption amount must be positive.");
        }
        if (user.getRewardPoints() < points) {
            throw new BadRequestException("Not enough reward points.");
        }
        user.setRewardPoints(user.getRewardPoints() - points);
        userRepository.save(user);

        rewardTransactionRepository.save(RewardTransaction.builder()
                .user(user)
                .points(-points)
                .entryType("redeem")
                .sourceModule("rewards")
                .description(description)
                .build());

        notifyUser(user, "Points redeemed", description, "info", "fa-gift");
    }

    public void notifyUser(User user, String title, String message, String category, String icon) {
        notificationRepository.save(Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .category(category)
                .icon(icon)
                .build());
    }

    public int getRulePoints(String module, int defaultValue) {
        return adminRewardRuleRepository.findByModule(module)
                .filter(AdminRewardRule::getActive)
                .map(AdminRewardRule::getPointsValue)
                .orElseGet(() -> rewardProperties.getDefaultRules().getOrDefault(module, defaultValue));
    }
}
