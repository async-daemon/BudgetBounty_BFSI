package com.budgetbounty.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "admin_reward_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRewardRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String module;

    @Column(length = 120)
    private String label;

    @Column(name = "points_value", nullable = false)
    private Integer pointsValue;

    @Column(length = 255)
    private String description;

    @Builder.Default
    @Column
    private Boolean active = true;
}
