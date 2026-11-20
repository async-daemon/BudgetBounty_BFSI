package com.budgetbounty.service;

import com.budgetbounty.dto.investment.CreateGoalRequest;
import com.budgetbounty.dto.investment.CreateInvestmentRequest;
import com.budgetbounty.dto.investment.InvestmentDto;
import com.budgetbounty.dto.investment.InvestmentGoalDto;
import com.budgetbounty.entity.Investment;
import com.budgetbounty.entity.InvestmentGoal;
import com.budgetbounty.entity.User;
import com.budgetbounty.exception.NotFoundException;
import com.budgetbounty.repository.InvestmentGoalRepository;
import com.budgetbounty.repository.InvestmentRepository;
import com.budgetbounty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final InvestmentGoalRepository investmentGoalRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;

    public List<InvestmentDto> listInvestments(Long userId) {
        return investmentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(InvestmentDto::from).toList();
    }

    public List<InvestmentGoalDto> listGoals(Long userId) {
        return investmentGoalRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(InvestmentGoalDto::from).toList();
    }

    @Transactional
    public InvestmentGoalDto createGoal(Long userId, CreateGoalRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        InvestmentGoal goal = InvestmentGoal.builder()
                .user(user)
                .name(request.getName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .build();
        investmentGoalRepository.save(goal);
        return InvestmentGoalDto.from(goal);
    }

    @Transactional
    public InvestmentDto createInvestment(Long userId, CreateInvestmentRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        InvestmentGoal goal = null;
        if (request.getGoalId() != null) {
            goal = investmentGoalRepository.findByIdAndUserId(request.getGoalId(), userId)
                    .orElseThrow(() -> new NotFoundException("Goal not found"));
        }

        Investment investment = Investment.builder()
                .user(user)
                .goal(goal)
                .name(request.getName())
                .assetType(request.getAssetType() == null ? "Mutual Fund" : request.getAssetType())
                .investedAmount(request.getInvestedAmount())
                .currentValue(request.getCurrentValue())
                .build();
        investmentRepository.save(investment);

        if (goal != null && goal.getPercentComplete() >= 100 && !"achieved".equals(goal.getStatus())) {
            goal.setStatus("achieved");
            int points = rewardService.getRulePoints("investment_goal", 75);
            rewardService.awardPoints(user, points, "investment_goal",
                    "Achieved investment goal: " + goal.getName());
            goal.setRewardPointsEarned(points);
            investmentGoalRepository.save(goal);
        }

        return InvestmentDto.from(investment);
    }
}
