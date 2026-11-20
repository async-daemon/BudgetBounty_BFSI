package com.budgetbounty.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "linked_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkedAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "institution_name", nullable = false, length = 120)
    private String institutionName;

    @Column(name = "account_type", nullable = false, length = 30)
    private String accountType; // bank | upi | investment | insurance | tax | property

    @Column(name = "account_number_masked", length = 40)
    private String accountNumberMasked;

    @Column(name = "upi_id", length = 80)
    private String upiId;

    @Builder.Default
    @Column(name = "balance")
    private Double balance = 0.0;

    @Builder.Default
    @Column(length = 20)
    private String status = "active";

    @Builder.Default
    @Column(name = "linked_at")
    private LocalDateTime linkedAt = LocalDateTime.now();
}
