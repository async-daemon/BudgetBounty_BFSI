package com.budgetbounty.repository;

import com.budgetbounty.entity.BudgetExpense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetExpenseRepository extends JpaRepository<BudgetExpense, Long> {
}
