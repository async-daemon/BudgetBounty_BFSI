package com.budgetbounty.dto.investment;

import com.budgetbounty.entity.InvestmentGoal;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class InvestmentGoalDto {
    private Long id;
    private String name;
    private Double targetAmount;
    private LocalDate targetDate;
    private String status;
    private Integer rewardPointsEarned;
    private double currentAmount;
    private int percentComplete;

    public static InvestmentGoalDto from(InvestmentGoal g) {
        return new InvestmentGoalDto(g.getId(), g.getName(), g.getTargetAmount(), g.getTargetDate(), g.getStatus(),
                g.getRewardPointsEarned(), g.getCurrentAmount(), g.getPercentComplete());
    }
}
