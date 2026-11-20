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
@Table(name = "insurance_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsurancePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String provider;

    @Column(name = "policy_number", nullable = false, length = 60)
    private String policyNumber;

    @Builder.Default
    @Column(name = "policy_type", length = 40)
    private String policyType = "Life"; // Life | Health | Vehicle | Home

    @Column(name = "premium_amount", nullable = false)
    private Double premiumAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Builder.Default
    @Column(length = 20)
    private String status = "active";

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InsurancePayment> payments = new ArrayList<>();
}
