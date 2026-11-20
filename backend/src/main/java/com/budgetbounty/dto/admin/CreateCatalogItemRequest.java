package com.budgetbounty.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCatalogItemRequest {
    @NotBlank
    private String name;

    private String description;
    private String category;
    private String partnerName;

    @Positive
    private Integer pointsRequired;

    private String icon;
}
