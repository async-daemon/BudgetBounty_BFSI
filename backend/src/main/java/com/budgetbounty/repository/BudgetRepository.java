package com.budgetbounty.repository;

import com.budgetbounty.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserIdAndPeriod(Long userId, String period);
    Optional<Budget> findByIdAndUserId(Long id, Long userId);
    Optional<Budget> findByUserIdAndCategoryAndPeriod(Long userId, String category, String period);
}
