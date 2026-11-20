package com.budgetbounty.repository;

import com.budgetbounty.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByUserIdOrderByDueDateAsc(Long userId);
    Optional<Bill> findByIdAndUserId(Long id, Long userId);
}
