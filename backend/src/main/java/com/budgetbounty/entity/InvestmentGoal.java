package com.budgetbounty.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "investment_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "target_amount", nullable = false)
    private Double targetAmount;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Builder.Default
    @Column(length = 20)
    private String status = "in_progress"; // in_progress | achieved

    @Builder.Default
    @Column(name = "reward_points_earned")
    private Integer rewardPointsEarned = 0;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "goal")
    private List<Investment> investments = new ArrayList<>();

    @Transient
    public double getCurrentAmount() {
        return investments.stream().mapToDouble(Investment::getCurrentValue).sum();
    }

    @Transient
    public int getPercentComplete() {
        if (targetAmount == null || targetAmount == 0) {
            return 0;
        }
        return (int) Math.min(Math.round((getCurrentAmount() / targetAmount) * 100), 100);
    }
}
