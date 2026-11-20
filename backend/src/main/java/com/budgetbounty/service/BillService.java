package com.budgetbounty.service;

import com.budgetbounty.dto.bill.BillDto;
import com.budgetbounty.dto.bill.CreateBillRequest;
import com.budgetbounty.entity.Bill;
import com.budgetbounty.entity.User;
import com.budgetbounty.exception.NotFoundException;
import com.budgetbounty.repository.BillRepository;
import com.budgetbounty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;

    @Transactional
    public List<BillDto> listForUser(Long userId) {
        List<Bill> bills = billRepository.findByUserIdOrderByDueDateAsc(userId);
        for (Bill b : bills) {
            if ("scheduled".equals(b.getStatus()) && b.isOverdue()) {
                b.setStatus("overdue");
            }
        }
        billRepository.saveAll(bills);
        return bills.stream().map(BillDto::from).toList();
    }

    @Transactional
    public BillDto create(Long userId, CreateBillRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        Bill bill = Bill.builder()
                .user(user)
                .name(request.getName())
                .category(request.getCategory() == null ? "Other" : request.getCategory())
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .recurring(request.isRecurring())
                .frequency(request.isRecurring() ? request.getFrequency() : null)
                .autopay(request.isAutopay())
                .build();
        billRepository.save(bill);

        rewardService.notifyUser(user, "Bill scheduled",
                bill.getName() + " (₹" + String.format("%,.0f", bill.getAmount()) + ") due on "
                        + bill.getDueDate(),
                "info", "fa-calendar-plus");

        return BillDto.from(bill);
    }

    @Transactional
    public BillDto pay(Long userId, Long billId) {
        Bill bill = billRepository.findByIdAndUserId(billId, userId)
                .orElseThrow(() -> new NotFoundException("Bill not found"));
        boolean wasTimely = !bill.getDueDate().isBefore(LocalDate.now());

        bill.setStatus("paid");
        bill.setPaidAt(LocalDateTime.now());

        if (wasTimely) {
            int points = rewardService.getRulePoints("bill_timely", 25);
            rewardService.awardPoints(bill.getUser(), points, "bill_timely", "Timely payment of " + bill.getName());
            bill.setRewardPointsEarned(points);
        }

        if (Boolean.TRUE.equals(bill.getRecurring())) {
            LocalDate nextDue = nextDueDate(bill.getDueDate(), bill.getFrequency());
            Bill nextBill = Bill.builder()
                    .user(bill.getUser())
                    .name(bill.getName())
                    .category(bill.getCategory())
                    .amount(bill.getAmount())
                    .dueDate(nextDue)
                    .recurring(true)
                    .frequency(bill.getFrequency())
                    .autopay(bill.getAutopay())
                    .build();
            billRepository.save(nextBill);
        }

        billRepository.save(bill);
        return BillDto.from(bill);
    }

    @Transactional
    public BillDto cancel(Long userId, Long billId) {
        Bill bill = billRepository.findByIdAndUserId(billId, userId)
                .orElseThrow(() -> new NotFoundException("Bill not found"));
        bill.setStatus("cancelled");
        billRepository.save(bill);
        return BillDto.from(bill);
    }

    private LocalDate nextDueDate(LocalDate current, String frequency) {
        if (frequency == null) {
            return current.plusDays(30);
        }
        return switch (frequency) {
            case "weekly" -> current.plusWeeks(1);
            case "quarterly" -> current.plusDays(91);
            case "yearly" -> current.plusDays(365);
            default -> current.plusDays(30);
        };
    }
}
