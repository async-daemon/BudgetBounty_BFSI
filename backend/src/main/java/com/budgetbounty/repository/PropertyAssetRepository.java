package com.budgetbounty.repository;

import com.budgetbounty.entity.PropertyAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyAssetRepository extends JpaRepository<PropertyAsset, Long> {
    List<PropertyAsset> findByUserId(Long userId);
}
