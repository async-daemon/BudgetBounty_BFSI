package com.budgetbounty.repository;

import com.budgetbounty.entity.TaxCalculation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaxCalculationRepository extends JpaRepository<TaxCalculation, Long> {
    List<TaxCalculation> findByUserIdOrderByCreatedAtDesc(Long userId);
}
