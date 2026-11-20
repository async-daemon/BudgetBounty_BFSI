package com.budgetbounty.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private InvestmentGoal goal;

    @Column(nullable = false, length = 120)
    private String name;

    @Builder.Default
    @Column(name = "asset_type", length = 40)
    private String assetType = "Mutual Fund"; // Stocks | Bonds | Mutual Fund | FD | Gold

    @Column(name = "invested_amount", nullable = false)
    private Double investedAmount;

    @Column(name = "current_value", nullable = false)
    private Double currentValue;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    public double getGainLoss() {
        return currentValue - investedAmount;
    }

    @Transient
    public double getGainLossPercent() {
        if (investedAmount == null || investedAmount == 0) {
            return 0;
        }
        return Math.round((getGainLoss() / investedAmount) * 10000) / 100.0;
    }
}
