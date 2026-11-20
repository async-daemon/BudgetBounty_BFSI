package com.budgetbounty.repository;

import com.budgetbounty.entity.AdminRewardRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRewardRuleRepository extends JpaRepository<AdminRewardRule, Long> {
    Optional<AdminRewardRule> findByModule(String module);
    List<AdminRewardRule> findAllByOrderByModuleAsc();
}
