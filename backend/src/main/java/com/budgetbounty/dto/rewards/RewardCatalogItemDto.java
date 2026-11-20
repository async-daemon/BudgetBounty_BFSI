package com.budgetbounty.dto.rewards;

import com.budgetbounty.entity.RewardCatalogItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RewardCatalogItemDto {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String partnerName;
    private Integer pointsRequired;
    private String icon;

    public static RewardCatalogItemDto from(RewardCatalogItem i) {
        return new RewardCatalogItemDto(i.getId(), i.getName(), i.getDescription(), i.getCategory(),
                i.getPartnerName(), i.getPointsRequired(), i.getIcon());
    }
}
