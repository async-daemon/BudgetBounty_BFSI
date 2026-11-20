package com.budgetbounty.service;

import com.budgetbounty.dto.insurance.CreatePolicyRequest;
import com.budgetbounty.dto.insurance.InsurancePolicyDto;
import com.budgetbounty.entity.InsurancePayment;
import com.budgetbounty.entity.InsurancePolicy;
import com.budgetbounty.entity.User;
import com.budgetbounty.exception.NotFoundException;
import com.budgetbounty.repository.InsurancePaymentRepository;
import com.budgetbounty.repository.InsurancePolicyRepository;
import com.budgetbounty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InsuranceService {

    private final InsurancePolicyRepository insurancePolicyRepository;
    private final InsurancePaymentRepository insurancePaymentRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;

    public List<InsurancePolicyDto> listPolicies(Long userId) {
        return insurancePolicyRepository.findByUserId(userId).stream().map(InsurancePolicyDto::from).toList();
    }

    @Transactional
    public InsurancePolicyDto createPolicy(Long userId, CreatePolicyRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        InsurancePolicy policy = InsurancePolicy.builder()
                .user(user)
                .provider(request.getProvider())
                .policyNumber(request.getPolicyNumber())
                .policyType(request.getPolicyType() == null ? "Life" : request.getPolicyType())
                .premiumAmount(request.getPremiumAmount())
                .dueDate(request.getDueDate())
                .build();
        insurancePolicyRepository.save(policy);
        return InsurancePolicyDto.from(policy);
    }

    @Transactional
    public InsurancePolicyDto payPremium(Long userId, Long policyId) {
        InsurancePolicy policy = insurancePolicyRepository.findByIdAndUserId(policyId, userId)
                .orElseThrow(() -> new NotFoundException("Policy not found"));

        int points = rewardService.getRulePoints("insurance_payment", 30);
        InsurancePayment payment = InsurancePayment.builder()
                .policy(policy)
                .amount(policy.getPremiumAmount())
                .status("paid")
                .rewardPointsEarned(points)
                .build();
        insurancePaymentRepository.save(payment);

        rewardService.awardPoints(policy.getUser(), points, "insurance_payment",
                policy.getPolicyType() + " premium for " + policy.getProvider());

        return InsurancePolicyDto.from(policy);
    }
}
