package com.budgetbounty.repository;

import com.budgetbounty.entity.InsurancePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InsurancePaymentRepository extends JpaRepository<InsurancePayment, Long> {
    @Query("select p from InsurancePayment p where p.id = :id and p.policy.user.id = :userId")
    Optional<InsurancePayment> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
