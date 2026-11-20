package com.budgetbounty.repository;

import com.budgetbounty.entity.Redemption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RedemptionRepository extends JpaRepository<Redemption, Long> {
    List<Redemption> findByUserIdOrderByRedeemedAtDesc(Long userId);
}
