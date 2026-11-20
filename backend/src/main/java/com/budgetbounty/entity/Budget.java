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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 60)
    private String category;

    @Column(name = "monthly_limit", nullable = false)
    private Double monthlyLimit;

    @Builder.Default
    @Column(name = "spent_amount")
    private Double spentAmount = 0.0;

    @Builder.Default
    @Column(length = 7)
    private String period = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

    @Builder.Default
    @Column(name = "reward_points_earned")
    private Integer rewardPointsEarned = 0;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BudgetExpense> expenses = new ArrayList<>();

    @Transient
    public int getPercentUsed() {
        if (monthlyLimit == null || monthlyLimit == 0) {
            return 0;
        }
        return (int) Math.min(Math.round((spentAmount / monthlyLimit) * 100), 999);
    }

    @Transient
    public boolean isOverBudget() {
        return spentAmount != null && monthlyLimit != null && spentAmount > monthlyLimit;
    }
}
