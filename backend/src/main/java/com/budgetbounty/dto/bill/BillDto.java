package com.budgetbounty.dto.bill;

import com.budgetbounty.entity.Bill;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BillDto {
    private Long id;
    private String name;
    private String category;
    private Double amount;
    private LocalDate dueDate;
    private boolean recurring;
    private String frequency;
    private boolean autopay;
    private String status;
    private Integer rewardPointsEarned;
    private LocalDateTime paidAt;
    private boolean overdue;

    public static BillDto from(Bill b) {
        return new BillDto(b.getId(), b.getName(), b.getCategory(), b.getAmount(), b.getDueDate(),
                Boolean.TRUE.equals(b.getRecurring()), b.getFrequency(), Boolean.TRUE.equals(b.getAutopay()),
                b.getStatus(), b.getRewardPointsEarned(), b.getPaidAt(), b.isOverdue());
    }
}
