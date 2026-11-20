package com.budgetbounty.service;

import com.budgetbounty.dto.admin.*;
import com.budgetbounty.dto.property.PropertyPaymentDto;
import com.budgetbounty.dto.transfer.TransactionDto;
import com.budgetbounty.entity.AdminRewardRule;
import com.budgetbounty.entity.PropertyPayment;
import com.budgetbounty.entity.RewardCatalogItem;
import com.budgetbounty.exception.NotFoundException;
import com.budgetbounty.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AdminRewardRuleRepository rewardRuleRepository;
    private final RewardCatalogItemRepository catalogItemRepository;
    private final TransactionRepository transactionRepository;
    private final PropertyPaymentRepository propertyPaymentRepository;

    public List<AdminUserDto> listUsers() {
        return userRepository.findAll().stream().map(AdminUserDto::from).toList();
    }

    public List<AdminRewardRuleDto> listRewardRules() {
        return rewardRuleRepository.findAllByOrderByModuleAsc().stream().map(AdminRewardRuleDto::from).toList();
    }

    @Transactional
    public AdminRewardRuleDto updateRewardRule(String module, UpdateRewardRuleRequest request) {
        AdminRewardRule rule = rewardRuleRepository.findByModule(module)
                .orElseThrow(() -> new NotFoundException("Reward rule not found for module " + module));
        rule.setPointsValue(request.getPointsValue());
        rule.setActive(request.isActive());
        rewardRuleRepository.save(rule);
        return AdminRewardRuleDto.from(rule);
    }

    public List<CatalogItemAdminDto> listAllCatalogItems() {
        return catalogItemRepository.findAll().stream().map(CatalogItemAdminDto::from).toList();
    }

    @Transactional
    public CatalogItemAdminDto createCatalogItem(CreateCatalogItemRequest request) {
        RewardCatalogItem item = RewardCatalogItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory() == null ? "giftcard" : request.getCategory())
                .partnerName(request.getPartnerName())
                .pointsRequired(request.getPointsRequired())
                .icon(request.getIcon() == null ? "fa-gift" : request.getIcon())
                .active(true)
                .build();
        catalogItemRepository.save(item);
        return CatalogItemAdminDto.from(item);
    }

    @Transactional
    public void toggleCatalogItem(Long itemId, boolean active) {
        RewardCatalogItem item = catalogItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Catalog item not found"));
        item.setActive(active);
        catalogItemRepository.save(item);
    }

    public List<TransactionDto> listAllTransactions() {
        return transactionRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(TransactionDto::from).toList();
    }

    public List<PropertyPaymentDto> listPendingPropertyApprovals() {
        return propertyPaymentRepository.findAll().stream()
                .filter(p -> "pending".equals(p.getStatus()))
                .map(PropertyPaymentDto::from).toList();
    }

    @Transactional
    public PropertyPaymentDto approvePropertyPayment(Long paymentId) {
        PropertyPayment payment = propertyPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        payment.setStatus("approved");
        propertyPaymentRepository.save(payment);
        return PropertyPaymentDto.from(payment);
    }

    @Transactional
    public PropertyPaymentDto rejectPropertyPayment(Long paymentId) {
        PropertyPayment payment = propertyPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        payment.setStatus("rejected");
        propertyPaymentRepository.save(payment);
        return PropertyPaymentDto.from(payment);
    }
}
