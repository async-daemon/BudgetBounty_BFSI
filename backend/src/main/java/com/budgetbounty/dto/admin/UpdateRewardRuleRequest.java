package com.budgetbounty.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRewardRuleRequest {
    @NotNull
    @PositiveOrZero
    private Integer pointsValue;

    private boolean active = true;
}
