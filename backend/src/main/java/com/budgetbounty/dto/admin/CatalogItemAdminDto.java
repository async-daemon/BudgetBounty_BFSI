package com.budgetbounty.dto.admin;

import com.budgetbounty.entity.RewardCatalogItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CatalogItemAdminDto {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String partnerName;
    private Integer pointsRequired;
    private String icon;
    private boolean active;

    public static CatalogItemAdminDto from(RewardCatalogItem i) {
        return new CatalogItemAdminDto(i.getId(), i.getName(), i.getDescription(), i.getCategory(),
                i.getPartnerName(), i.getPointsRequired(), i.getIcon(), Boolean.TRUE.equals(i.getActive()));
    }
}
