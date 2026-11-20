package com.budgetbounty.dto.property;

import com.budgetbounty.entity.PropertyAsset;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PropertyDto {
    private Long id;
    private String name;
    private String address;
    private String propertyManager;

    public static PropertyDto from(PropertyAsset p) {
        return new PropertyDto(p.getId(), p.getName(), p.getAddress(), p.getPropertyManager());
    }
}
