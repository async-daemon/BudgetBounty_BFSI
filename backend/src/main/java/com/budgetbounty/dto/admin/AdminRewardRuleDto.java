package com.budgetbounty.dto.admin;

import com.budgetbounty.entity.AdminRewardRule;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminRewardRuleDto {
    private Long id;
    private String module;
    private String label;
    private Integer pointsValue;
    private String description;
    private boolean active;

    public static AdminRewardRuleDto from(AdminRewardRule r) {
        return new AdminRewardRuleDto(r.getId(), r.getModule(), r.getLabel(), r.getPointsValue(),
                r.getDescription(), Boolean.TRUE.equals(r.getActive()));
    }
}
