package com.budgetbounty.repository;

import com.budgetbounty.entity.LinkedAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LinkedAccountRepository extends JpaRepository<LinkedAccount, Long> {
    List<LinkedAccount> findByUserIdOrderByLinkedAtDesc(Long userId);
}
