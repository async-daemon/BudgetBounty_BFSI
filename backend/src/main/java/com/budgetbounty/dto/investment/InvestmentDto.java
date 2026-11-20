package com.budgetbounty.dto.investment;

import com.budgetbounty.entity.Investment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvestmentDto {
    private Long id;
    private Long goalId;
    private String name;
    private String assetType;
    private Double investedAmount;
    private Double currentValue;
    private double gainLoss;
    private double gainLossPercent;

    public static InvestmentDto from(Investment i) {
        return new InvestmentDto(i.getId(), i.getGoal() == null ? null : i.getGoal().getId(), i.getName(),
                i.getAssetType(), i.getInvestedAmount(), i.getCurrentValue(), i.getGainLoss(),
                i.getGainLossPercent());
    }
}
