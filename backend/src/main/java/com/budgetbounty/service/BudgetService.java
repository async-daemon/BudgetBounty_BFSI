package com.budgetbounty.service;

import com.budgetbounty.dto.budget.BudgetDto;
import com.budgetbounty.dto.budget.CreateBudgetRequest;
import com.budgetbounty.dto.budget.LogExpenseRequest;
import com.budgetbounty.entity.Budget;
import com.budgetbounty.entity.BudgetExpense;
import com.budgetbounty.entity.User;
import com.budgetbounty.exception.BadRequestException;
import com.budgetbounty.exception.NotFoundException;
import com.budgetbounty.repository.BudgetExpenseRepository;
import com.budgetbounty.repository.BudgetRepository;
import com.budgetbounty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;

    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    public List<BudgetDto> listForCurrentPeriod(Long userId) {
        String period = LocalDate.now().format(PERIOD_FORMAT);
        return budgetRepository.findByUserIdAndPeriod(userId, period).stream().map(BudgetDto::from).toList();
    }

    @Transactional
    public BudgetDto create(Long userId, CreateBudgetRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        String period = LocalDate.now().format(PERIOD_FORMAT);

        budgetRepository.findByUserIdAndCategoryAndPeriod(userId, request.getCategory(), period)
                .ifPresent(b -> {
                    throw new BadRequestException(
                            "A budget for " + request.getCategory() + " already exists this month. Edit it instead.");
                });

        Budget budget = Budget.builder()
                .user(user)
                .category(request.getCategory())
                .monthlyLimit(request.getMonthlyLimit())
                .spentAmount(0.0)
                .period(period)
                .build();
        budgetRepository.save(budget);
        return BudgetDto.from(budget);
    }

    @Transactional
    public BudgetDto logExpense(Long userId, Long budgetId, LogExpenseRequest request) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new NotFoundException("Budget not found"));
        User user = budget.getUser();

        BudgetExpense expense = BudgetExpense.builder()
                .budget(budget)
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();
        budget.getExpenses().add(expense);
        budget.setSpentAmount((budget.getSpentAmount() == null ? 0 : budget.getSpentAmount()) + request.getAmount());

        if (budget.getSpentAmount() > budget.getMonthlyLimit()) {
            rewardService.notifyUser(user, "Budget exceeded!",
                    "You've gone over your " + budget.getCategory() + " budget (₹"
                            + String.format("%,.0f", budget.getSpentAmount()) + " / ₹"
                            + String.format("%,.0f", budget.getMonthlyLimit()) + ").",
                    "warning", "fa-triangle-exclamation");
        } else if (budget.getPercentUsed() >= 80) {
            rewardService.notifyUser(user, "Budget alert",
                    "You've used " + budget.getPercentUsed() + "% of your " + budget.getCategory() + " budget.",
                    "warning", "fa-bell");
        }

        budgetRepository.save(budget);
        return BudgetDto.from(budget);
    }

    @Transactional
    public BudgetDto closePeriod(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new NotFoundException("Budget not found"));

        if (!budget.isOverBudget()) {
            int points = rewardService.getRulePoints("budget_adherence", 50);
            rewardService.awardPoints(budget.getUser(), points, "budget_adherence",
                    "Stayed within " + budget.getCategory() + " budget for " + budget.getPeriod());
            budget.setRewardPointsEarned(points);
            budgetRepository.save(budget);
        }
        return BudgetDto.from(budget);
    }

    @Transactional
    public void delete(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new NotFoundException("Budget not found"));
        budgetRepository.delete(budget);
    }
}
