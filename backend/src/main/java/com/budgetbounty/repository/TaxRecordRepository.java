package com.budgetbounty.repository;

import com.budgetbounty.entity.TaxRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaxRecordRepository extends JpaRepository<TaxRecord, Long> {
    List<TaxRecord> findByUserIdOrderByDueDateAsc(Long userId);
    Optional<TaxRecord> findByIdAndUserId(Long id, Long userId);
}
