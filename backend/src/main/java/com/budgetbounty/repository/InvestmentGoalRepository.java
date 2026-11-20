package com.budgetbounty.repository;

import com.budgetbounty.entity.InvestmentGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvestmentGoalRepository extends JpaRepository<InvestmentGoal, Long> {
    List<InvestmentGoal> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<InvestmentGoal> findByIdAndUserId(Long id, Long userId);
}
