package com.budgetbounty.repository;

import com.budgetbounty.entity.RewardCatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardCatalogItemRepository extends JpaRepository<RewardCatalogItem, Long> {
    List<RewardCatalogItem> findByActiveTrue();
}
