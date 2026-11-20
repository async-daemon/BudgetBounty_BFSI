package com.budgetbounty.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reward_catalog_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardCatalogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 255)
    private String description;

    @Builder.Default
    @Column(length = 30)
    private String category = "giftcard"; // giftcard | discount | cashback

    @Column(name = "partner_name", length = 120)
    private String partnerName;

    @Column(name = "points_required", nullable = false)
    private Integer pointsRequired;

    @Builder.Default
    @Column(length = 40)
    private String icon = "fa-gift";

    @Builder.Default
    @Column
    private Boolean active = true;
}
