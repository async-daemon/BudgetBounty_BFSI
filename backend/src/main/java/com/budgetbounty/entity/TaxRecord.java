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
@Table(name = "tax_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "assessment_year", nullable = false, length = 10)
    private String assessmentYear;

    @Builder.Default
    @Column(name = "tax_type", length = 40)
    private String taxType = "Income Tax";

    @Column(name = "amount_due", nullable = false)
    private Double amountDue;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Builder.Default
    @Column(length = 20)
    private String status = "pending"; // pending | paid | overdue

    @Builder.Default
    @Column(name = "reward_points_earned")
    private Integer rewardPointsEarned = 0;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
