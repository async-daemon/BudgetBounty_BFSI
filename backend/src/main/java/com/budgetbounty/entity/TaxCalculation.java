package com.budgetbounty.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tax_calculations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "annual_income", nullable = false)
    private Double annualIncome;

    @Builder.Default
    @Column(length = 10)
    private String regime = "new"; // old | new

    @Builder.Default
    @Column
    private Double deductions = 0.0;

    @Column(name = "taxable_income", nullable = false)
    private Double taxableIncome;

    @Column(name = "tax_payable", nullable = false)
    private Double taxPayable;

    @Builder.Default
    @Column
    private Double cess = 0.0;

    @Column(name = "total_payable", nullable = false)
    private Double totalPayable;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
