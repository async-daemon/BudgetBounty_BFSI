package com.budgetbounty.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String name;

    @Builder.Default
    @Column(length = 60)
    private String category = "Utilities";

    @Column(nullable = false)
    private Double amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Builder.Default
    @Column
    private Boolean recurring = false;

    @Column(length = 20)
    private String frequency; // weekly | monthly | quarterly | yearly

    @Builder.Default
    @Column
    private Boolean autopay = false;

    @Builder.Default
    @Column(length = 20)
    private String status = "scheduled"; // scheduled | paid | overdue | cancelled

    @Builder.Default
    @Column(name = "reward_points_earned")
    private Integer rewardPointsEarned = 0;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Transient
    public boolean isOverdue() {
        return "scheduled".equals(status) && dueDate != null && dueDate.isBefore(LocalDate.now());
    }
}
