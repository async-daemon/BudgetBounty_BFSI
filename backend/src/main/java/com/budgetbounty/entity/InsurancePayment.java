package com.budgetbounty.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "insurance_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsurancePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private InsurancePolicy policy;

    @Column(nullable = false)
    private Double amount;

    @Builder.Default
    @Column(length = 20)
    private String status = "paid";

    @Builder.Default
    @Column(name = "reward_points_earned")
    private Integer rewardPointsEarned = 0;

    @Builder.Default
    @Column(name = "paid_at")
    private LocalDateTime paidAt = LocalDateTime.now();
}
