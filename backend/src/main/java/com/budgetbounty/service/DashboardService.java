package com.budgetbounty.service;

import com.budgetbounty.dto.dashboard.*;
import com.budgetbounty.entity.LinkedAccount;
import com.budgetbounty.entity.User;
import com.budgetbounty.exception.NotFoundException;
import com.budgetbounty.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final LinkedAccountRepository linkedAccountRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final BillRepository billRepository;
    private final InvestmentRepository investmentRepository;
    private final PropertyPaymentRepository propertyPaymentRepository;
    private final InsurancePolicyRepository insurancePolicyRepository;
    private final TaxRecordRepository taxRecordRepository;
    private final DocumentRepository documentRepository;
    private final NotificationRepository notificationRepository;

    public DashboardOverviewDto overview(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        String period = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        double totalTransferred = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(t -> "success".equals(t.getStatus()))
                .mapToDouble(com.budgetbounty.entity.Transaction::getAmount).sum();

        double totalBudgetSpent = budgetRepository.findByUserIdAndPeriod(userId, period).stream()
                .mapToDouble(b -> b.getSpentAmount() == null ? 0 : b.getSpentAmount()).sum();

        long upcomingBills = billRepository.findByUserIdOrderByDueDateAsc(userId).stream()
                .filter(b -> "scheduled".equals(b.getStatus()) || "overdue".equals(b.getStatus()))
                .count();

        double investmentValue = investmentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .mapToDouble(com.budgetbounty.entity.Investment::getCurrentValue).sum();

        long pendingPropertyPayments = propertyPaymentRepository.findAllForUser(userId).stream()
                .filter(p -> "pending".equals(p.getStatus()) || "approved".equals(p.getStatus()))
                .count();

        long activeInsurance = insurancePolicyRepository.findByUserId(userId).stream()
                .filter(p -> "active".equals(p.getStatus())).count();

        long pendingTax = taxRecordRepository.findByUserIdOrderByDueDateAsc(userId).stream()
                .filter(r -> !"paid".equals(r.getStatus())).count();

        long documentsCount = documentRepository.findByUserIdOrderByUploadedAtDesc(userId).size();

        long unreadNotif = notificationRepository.countByUserIdAndIsReadFalse(userId);

        return new DashboardOverviewDto(user.getRewardPoints(), totalTransferred, totalBudgetSpent, upcomingBills,
                investmentValue, pendingPropertyPayments, activeInsurance, pendingTax, documentsCount, unreadNotif);
    }

    public List<LinkedAccountDto> listLinkedAccounts(Long userId) {
        return linkedAccountRepository.findByUserIdOrderByLinkedAtDesc(userId).stream()
                .map(LinkedAccountDto::from).toList();
    }

    @Transactional
    public LinkedAccountDto linkAccount(Long userId, LinkAccountRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        LinkedAccount account = LinkedAccount.builder()
                .user(user)
                .institutionName(request.getInstitutionName())
                .accountType(request.getAccountType())
                .accountNumberMasked(request.getAccountNumberMasked())
                .upiId(request.getUpiId())
                .balance(0.0)
                .status("active")
                .build();
        linkedAccountRepository.save(account);
        return LinkedAccountDto.from(account);
    }

    @Transactional
    public void unlinkAccount(Long userId, Long accountId) {
        LinkedAccount account = linkedAccountRepository.findById(accountId)
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Linked account not found"));
        linkedAccountRepository.delete(account);
    }

    public List<NotificationDto> listNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationDto::from).toList();
    }

    @Transactional
    public void markNotificationsRead(Long userId) {
        List<com.budgetbounty.entity.Notification> unread = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(n -> !Boolean.TRUE.equals(n.getIsRead())).toList();
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }
}
