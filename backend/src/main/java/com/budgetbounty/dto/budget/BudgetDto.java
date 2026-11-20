package com.budgetbounty.dto.budget;

import com.budgetbounty.entity.Budget;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BudgetDto {
    private Long id;
    private String category;
    private Double monthlyLimit;
    private Double spentAmount;
    private String period;
    private Integer rewardPointsEarned;
    private int percentUsed;
    private boolean overBudget;

    public static BudgetDto from(Budget b) {
        return new BudgetDto(b.getId(), b.getCategory(), b.getMonthlyLimit(), b.getSpentAmount(), b.getPeriod(),
                b.getRewardPointsEarned(), b.getPercentUsed(), b.isOverBudget());
    }
}
